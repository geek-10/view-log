#!/usr/bin/env python3

import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
import Adafruit_DHT
from datetime import date,datetime
import time
import csv

class TemperatureLogger():
    def __init__(self):
        self.once = True
        self.previousDate = ""
        self.fetchSignal = False
        self.fetchEntries = False
        self.onceConnect = True
        # self.fetchtime = time.time()
        self.signalOnce = True
        self.writeOnce = True
        self.no_of_lines = 0
        self.signalReceiver()

    def signalReceiver(self):
        # self.previousFetch = False
        while True:
            if self.onceConnect:
                self.connectToDatabase()
                ref = db.reference('/signal')
                self.fetchSignal = ref.get()
                self.onceConnect = False
                # self.previousFetch = self.fetchSignal
            else:
                ref = db.reference('/signal')
                self.fetchSignal = ref.get()
            if self.fetchSignal['signal']:
                self.timeForDelay = float(self.fetchSignal['timeForDelay'])
                self.runLogger()
                # self.previousFetch = self.fetchSignal
            time.sleep(1)

    def runLogger(self):
        if self.once:
            self.fetchtime = time.time()
            self.databaseWrite()
        else:
            if (time.time() - self.fetchtime) >= (self.timeForDelay * 60):
                self.databaseWrite()
                self.fetchtime = time.time()


    def connectToDatabase(self):
        cred = credentials.Certificate('path-to-service-account-key.json')
        firebase_admin.initialize_app(cred,{
            'databaseURL': 'path-to-database-url'
        })

        if self.signalOnce:
            ref = db.reference()
            ref.child('signal').set({
                'signal':False,
                'timeForDelay': 1,
            })
            ref.child('Entries').set({
                'Previous-Entries': 0
            })
            self.signalOnce = False


    def currentDateFunc(self):
        currentDate = date.today()
        currentDate = currentDate.strftime('%d-%m-%Y')
        info = datetime.now()
        time = info.strftime("%H-%M")
        return currentDate, time

    def databaseWrite(self):
        newDate,timeOfTemp = self.currentDateFunc()

        if self.no_of_lines >= 10000:
            self.historyObject.delete()
            self.historyObject = db.reference("/history" + "/" + self.folderName)
            valueHolder = int(self.no_of_lines/10000)
            self.no_of_lines = self.no_of_lines - (valueHolder * 10000)
            previousRef = db.reference("/Entries")
            previousRef.set({
                "Previous-Entries":self.no_of_lines
            })
        
        if self.previousDate == newDate:
            self.temperature = self.getTempData()
            self.historyObject.child(timeOfTemp).push().set({
                "Temperature": self.temperature
            })
            self.fileObject.set({
                timeOfTemp: self.temperature
            })
            self.storeData(self.previousDate,timeOfTemp,self.temperature)
            previousRef = db.reference("/Entries")
            previousRef.set({
                "Previous-Entries":self.no_of_lines
            })
        else:
            self.previousDate = newDate
            self.folderName = newDate
            path = '/' + self.folderName
            self.fileObject = db.reference(path)
            self.historyObject = db.reference("/history"+"/"+self.folderName)
            if self.once:
                self.temperature = self.getTempData()
                self.historyObject.child(timeOfTemp).push().set({
                    "Temperature": self.temperature
                })
                self.fileObject.set({
                    timeOfTemp: self.temperature
                })
                self.storeData(self.folderName, timeOfTemp, self.temperature)
                self.once = False

    def getTempData(self):
        sensor = Adafruit_DHT.DHT11
        sensorPin = 21
        valid_input = False
        while not valid_input:
            humidity, temperature = Adafruit_DHT.read(sensor,sensorPin)
            if self.is_valid(temperature):
                valid_input = True
                return int(temperature)

    def is_valid(self,value):
        if value is not None:
            return True
        else:
            return False
        
    def storeData(self,date,time,temperature):
        with open('/home/jarvis/Desktop/view-log/View-Log.csv','a') as file:
            if self.writeOnce:
                file.write("Date, Time, Temperature\n")
                self.writeOnce = False
            writer = csv.DictWriter(file,fieldnames =["Date", "Time", "Temperature"])
            writer.writerow({"Date": date, "Time": time, "Temperature": temperature})
        reader = csv.reader(open('View-Log.csv'))
        self.no_of_lines = len(list(reader))


sensor = TemperatureLogger()

