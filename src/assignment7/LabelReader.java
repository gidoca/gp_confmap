package assignment7;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import javax.vecmath.Point2f;

public class LabelReader {
	Scanner lblScanner;
	Scanner txcScanner;
	
	public LabelReader(String lblFilename, String txcFilename) throws FileNotFoundException {
		lblScanner = new Scanner(new File(lblFilename));
		txcScanner = new Scanner(new File(txcFilename));
	}
	
	public HashMap<Integer, Point2f> read()
	{
		int index;
		String labelname;
		
		HashMap<String, Integer> lbl = new HashMap<String, Integer>();
		
		while(lblScanner.hasNext())
		{
			index = lblScanner.nextInt();
			labelname = lblScanner.next();
			
			System.out.println("" + index + "=>" + labelname);
			lbl.put(labelname, index);
		}
		
		HashMap<Integer, Point2f> out = new HashMap<Integer, Point2f>();
		
		while(txcScanner.hasNext())
		{
			Point2f p = new Point2f();
			p.x = txcScanner.nextFloat();
			p.y = txcScanner.nextFloat();
			labelname = txcScanner.next();
			out.put(lbl.get(labelname), p);
		}
		
		return out;
	}
}
