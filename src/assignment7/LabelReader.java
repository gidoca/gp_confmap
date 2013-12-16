package assignment7;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Scanner;

import javax.vecmath.Point2f;

public class LabelReader {
	Scanner lblScanner;
	Scanner txcScanner;
	public LinkedHashMap<String, Integer> lbl;
	
	public LabelReader(String lblFilename, String txcFilename) throws FileNotFoundException {
		lblScanner = new Scanner(new File(lblFilename));
		txcScanner = new Scanner(new File(txcFilename));
	}
	
	public LinkedHashMap<Integer, Point2f> read()
	{
		int index;
		String labelname;
		
		lbl = new LinkedHashMap<String, Integer>();
		
		while(lblScanner.hasNext())
		{
			if(!lblScanner.hasNextInt())
			{
				lblScanner.nextLine();
				continue;
			}
			index = lblScanner.nextInt();
			labelname = lblScanner.next();
			
			//System.out.println("" + index + "=>" + labelname);
			lbl.put(labelname, index);
		}
		
		LinkedHashMap<Integer, Point2f> out = new LinkedHashMap<Integer, Point2f>();
		
		while(txcScanner.hasNext())
		{
			if(!txcScanner.hasNextFloat())
			{
				txcScanner.nextLine();
				continue;
			}
			Point2f p = new Point2f();
			p.x = txcScanner.nextFloat();
			p.y = txcScanner.nextFloat();
			labelname = txcScanner.next();
			assert(lbl.containsKey(labelname));
			out.put(lbl.get(labelname), p);
			//System.out.println(labelname + "=>" + p);
		}
		
		return out;
	}
}
