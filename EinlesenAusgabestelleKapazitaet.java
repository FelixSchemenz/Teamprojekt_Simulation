package krisenplanung_Simulation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class EinlesenAusgabestelleKapazitaet {
	/*Methode zum einlesen einer Excel Datei, welche die Vorräte der geöffneten Ausgabestellen bestimmt*/
	public static LinkedList<Integer> ausgabeKapazitaet() throws IOException {
		LinkedList <Integer> listeOffen = new LinkedList<Integer>();
		try {
		FileInputStream fis = new FileInputStream ("C:\\Users\\felix\\Documents\\AusgabestelleKapazitaet.xlsx");
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
		System.out.print(cell.getStringCellValue() + "\t\t\t");  
		break;  
		case Cell.CELL_TYPE_NUMERIC:    //field that represents number cell type  
			listeOffen.add( (int) cell.getNumericCellValue());
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
		return listeOffen;
		}
}
