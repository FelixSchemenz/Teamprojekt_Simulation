package krisenplanung_Simulation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class EinlesenZulieferer {
	/*Methode welche die Koordinaten und Anzahl der Zulieferer aus Excel-Datei einliest*/
	public static LinkedList <Location> einlesenZulieferer () throws IOException {
		LinkedList <Location> listeKoordinaten = new LinkedList<Location>();
		int zaehler = 0;
		double xkoordinate = -1;
		double ykoordinate = -1;
		int anzahl = 0;
		try {
		FileInputStream fis = new FileInputStream ("C:\\Users\\felix\\Documents\\Zulieferer_Koordinaten.xlsx");
		XSSFWorkbook wb=new XSSFWorkbook(fis); 
		XSSFSheet sheet=wb.getSheetAt(0);
		Iterator<Row> itr = sheet.iterator();    //iterating over excel file  
		while (itr.hasNext())                 
		{  
		Row row = itr.next();  
		Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column  
		while (cellIterator.hasNext())   
		{  
		Cell cell = cellIterator.next();  
		switch (cell.getCellType())               
		{  
		case Cell.CELL_TYPE_STRING:    //field that represents string cell type  
		/*System.out.print(cell.getStringCellValue() + "\t\t\t");  */
		break;  
		case Cell.CELL_TYPE_NUMERIC:    //field that represents number cell type  
			if (xkoordinate == -1) {
				xkoordinate = (double) cell.getNumericCellValue(); }
			else if (ykoordinate == -1){
				ykoordinate = (double) cell.getNumericCellValue();}
			else { 
				zaehler++;
				anzahl = (int) cell.getNumericCellValue();
				/*System.out.println("X-koordinate: " + xkoordinate + " Y-Koordinate: " + ykoordinate + " anzahl:" + anzahl);*/
				listeKoordinaten.add(new Location(xkoordinate,ykoordinate, anzahl));
				xkoordinate = -1;
				ykoordinate = -1;}
		/*System.out.print(cell.getNumericCellValue() + "\t\t\t");  */
		break;  
		default:  
		}  
		}
		System.out.println("");  
		}
		}
		catch(Exception e)  
		{
		e.printStackTrace();  
		}  
		System.out.println("Es wurden " + zaehler + "Koordinaten eingelesen");
		return listeKoordinaten;
		}
}
