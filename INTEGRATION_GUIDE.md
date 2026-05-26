# SmartFarm GUI — Integration Guide

## Files to copy into your IntelliJ project

```
src/main/java/com/farm/demo/
├── MainApp.java                        ← replace HelloApplication
├── view/
│   ├── MainLayout.java                 ← sidebar + content switcher
│   ├── ZonesView.java                  ← Zones Overview screen
│   ├── LivestockView.java              ← Livestock Registry screen
│   └── ProductionView.java             ← Production Records screen
├── controller/
│   ├── ZonesController.java
│   ├── LivestockController.java
│   └── ProductionController.java

src/main/resources/com/farm/demo/
└── styles.css                          ← dark theme stylesheet
```

---

## Step 1 — Update Launcher.java

Your existing Launcher.java should call MainApp, not HelloApplication:

```java
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
```

---

## Step 2 — Adjust model imports in controllers

The controllers use your existing model classes. You may need to adjust
the exact class names to match yours. Check these specifically:

### ZonesController uses:
- `Zone`, `CropZone`, `LivestockZone`, `AquacultureZone`
- `ZoneStatus`
- `HumiditySensor`, `RainfallSensor`, `DissolvedOxygen`, `BiometricSensor`

### ZonesView uses:
- `Zone`, `ZoneStatus`
- `CropZone`, `LivestockZone`, `AquacultureZone`
- `Sensor`, `SensorReading`

### LivestockController uses:
- `Animal`, `AnimalType`, `HealthStatus`, `HealthEvent`

### LivestockView uses:
- `Animal`, `AnimalType`, `HealthStatus`, `HealthEvent`

### ProductionController uses:
- `ProductionRecord`, `ProductionType`

---

## Step 3 — Methods your model must expose

The GUI calls these methods — check they exist in your classes:

### Zone
```java
String  getCode()
String  getName()           void setName(String)
ZoneStatus getStatus()      void setStatus(ZoneStatus)
String  getLocation()       void setLocation(String)
List<Sensor> getSensors()
void    addSensor(Sensor s)
void    suspend()           // should cascade to sensors
void    reactivate()
```

### Sensor
```java
List<SensorReading> getReadings()
void suspend()
void reactivate()
```

### SensorReading
```java
boolean isOutOfRange()
```

### Animal
```java
String      getId()
String      getSpecies()        void setSpecies(String)
AnimalType  getAnimalType()
double      getWeight()         void setWeight(double)
String      getTag()            void setTag(String)
HealthStatus getHealthStatus()  void setHealthStatus(HealthStatus)
double      getFeedConversionRatio()   void setFeedConversionRatio(double)
LocalDate   getNextVaccineDate()       void setNextVaccineDate(LocalDate)
List<HealthEvent> getHealthEvents()
void        addHealthEvent(HealthEvent e)
```

### HealthEvent
```java
LocalDate   getDate()
String      getEventType()
String      getAdministeredBy()
String      getNotes()
```

### ProductionRecord
```java
String          getZoneCode()
ProductionType  getProductionType()
double          getValue()
String          getUnit()
LocalDate       getDate()
```

---

## Step 4 — If a method name differs

Just rename it in the controller. For example if your Animal uses
`getHealthState()` instead of `getHealthStatus()`, change it in
LivestockController and LivestockView.

---

## Step 5 — module-info.java

Make sure your module-info.java opens the view and controller packages:

```java
module com.farm.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.chart;          // needed for BarChart in ProductionView

    opens com.farm.demo to javafx.fxml;
    opens com.farm.demo.view to javafx.fxml;
    opens com.farm.demo.controller to javafx.fxml;
    opens com.farm.demo.model to javafx.base;   // needed for TableView binding

    exports com.farm.demo;
    exports com.farm.demo.view;
    exports com.farm.demo.controller;
    exports com.farm.demo.model;
}
```

---

## Sample data is in the controllers

The three controllers each have a `loadSampleData()` method at the bottom.
This is where you replace hardcoded objects with real data from a file or DB.
The views automatically display whatever the controller returns from
`getAllZones()` / `getAllAnimals()` / `getAllRecords()`.
