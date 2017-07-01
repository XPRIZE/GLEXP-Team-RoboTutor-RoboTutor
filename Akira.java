import java.util.*;
import org.apache.poi.XSSF.usermodel.XSSFWorkbook;
import org.apache.poi.XSSF.usermodel.XSSFSheet;
import org.apache.poi.XSSF.usermodel.XSSFRow;
import org.apache.poi.XSSF.usermodel.XSSFCell;

import java.io.FileInputStream;
import java.io.IOException;

//Compile with Equation.java
public class Akira {

	//tester: main function
	public static void main(String[] args) throws FileNotFoundException {
		// file name
		String file = "evangelineliu/Documents/Robotutor/2017-04-27_AKIRA_arithmetic_packet_generator.xlsx";

		//putting excel sheet into array list as list of lists
		List allData = new ArrayList();
		FileInputStream f = new FileInputStream(file);
		XSSFWorkbook workbook = new XSSFWorkbook(f);
		XSSFSheet sheet = workbook.getSheetAt(0);
		Iterator rows = sheet.rowIterator();
        while (rows.hasNext()) {
            XSSFRow row = (XSSFRow) rows.next();
            Iterator cells = row.cellIterator();

            List data = new ArrayList();
            while (cells.hasNext()) {
                XSSFCell cell = (XSSFCell) cells.next();
                data.add(cell);
            }

            allData.add(data);
        }
        f.close();

        //do 1 cycle for testing
        Equation[][] eqs = make(allData, 1);

        //print for testing
        printEqs(eqs);
	}

	//print onto console for testing purposes
	public static void printEqs(Equation[][] eqs){
		for(int i = 0; i < eqs.size(); i++){
			for(int j = 0; j < eqs[i].size(); j++){
				eqs[i][j].printEq();
			}
		}
	}

	//generates equations using generator, puts into Array
	public static Equation[][] make(List data, int num_of_cycles){
		//Store sets of equations generated at each level in 2D array
		int levels = data.size();
		Equation[][] eqs = new Equation[num_of_cycles][levels];

		for(int i = 0; i < num_of_cycles; i++){
			for(int j = 1; j < levels; j++){
				//get data from row
				List row = (List) data.get(j);
				String ordered = ((XSSFCell) list.get(5)).getRichStringCellValue().getString();
				String op = ((XSSFCell) list.get(4)).getRichStringCellValue().getString();
				int minop1 = (int)Math.round(((XSSFCell) list.get(8)).getNumericCellValue());
				int maxop1 = (int)Math.round(((XSSFCell) list.get(9)).getNumericCellValue());
				int minop2 = (int)Math.round(((XSSFCell) list.get(10)).getNumericCellValue());
				int maxop2 = (int)Math.round(((XSSFCell) list.get(11)).getNumericCellValue());
				int unit = (int)Math.round(((XSSFCell) list.get(6)).getNumericCellValue());

				//what kind of problem to generate, incorporate into Equation object
				if(ordered.compare("random")){
					Equation e = generator(minop1, maxop1, minop2, maxop2, op, unit);
					eqs[i][j] = e;
				} else if (ordered.compare("up")) {
					Equation e = new Equation(minop1, maxop1, unit);
					eqs[i][j] = e;
				} else { //ordered.compare("down")
					Equation e = new Equation(minop1, maxop1, (-1 * unit));
					eqs[i][j] = e;
				}
 			}
		}

		return eqs;
	}
 
	public static Equation generator(int lo1, int hi1, int lo2, int hi2, String op, int unit) {
		//create a new Random object
		Random rand = new Random();
 
		//randomly generate 2 new operands 
		int op1 = ((rand.nextInt(hi1) + lo1) / unit) * unit;
		int op2 = (rand.nextInt(hi2) + lo2 / unit) * unit;

		if(op.compare("add/subtract")){
			int choose_op = rand.nextInt(2);
			//0 = subtraction, 1 = addition
			//can extend to multiplication and division if needed
 
			String op;
			if(choose_op == 0){
				op = "subtract";
			} else {
				op = "add";
			}

		} 
 
		Equation e = new Equation(op1, op2, op);
		return e;
	}
 
	//test if any carrying involved in addition/subtraction
	public static bool needs_carry(int x, int y, String op){
		while(x >= 10 && y >= 10){
			if(op.equal("add") && (x % 10) + (y % 10) >= 10){
				return true;
			} 
			if(op.equal("subtract") && (x % 10) < (y % 10)){
				return true;
			}
			x = x / 10;
			y = y / 10;
		}
		return false;
	}
}
