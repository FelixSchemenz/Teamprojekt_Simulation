# Teamprojekt_Simulation - Anleitung

1. Alle Dateien herunterladen
2. In Eclipse Repast Symphony ein neues Projekt z.B mit dem Namen Krisenplanung_Simulation erstellen
3. Java Klassen im src Ordner hinzufügen
4. Die Dateien vom Typ .cpg, .dbf, .prj, .shp und .shx in den Ordner data des neuen Projekts verschieben
5. Excel-Dateien lokal speichern und Dateipfade in den Einlesen-Methoden anpassen
6. Die context.xml Datei mit folgenden Code überschreiben:

   <context id="Krisenplanung_Simulation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xsi:noNamespaceSchemaLocation="http://repast.org/scenario/context">
  	<projection id="Geography" type="geography" />	
  </context> 

7. Fertig :)
