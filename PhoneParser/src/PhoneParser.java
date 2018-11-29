import java.awt.Color;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class PhoneParser {
	private static final BigDecimal INITIAL_COST = new BigDecimal("0.05");
	private static final BigDecimal AFTER_3_MIN_COST = new BigDecimal("0.03");
	MyHashMap nameNumberCost = new MyHashMap();
	ArrayList<PhoneRecord> phoneItems = new ArrayList<>();
	private void parseFile(String file) throws IOException {
		Reader in = new FileReader(file);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			PhoneRecord item = new PhoneRecord();
			item.name = record.get(0).trim();
			item.number = record.get(1).trim();
			item.time = record.get(2).trim();
			phoneItems.add(item);
		}
	}
	private void calculateInitialCost(PhoneRecord item) {
		String [] timeParts = item.time.split(":");
		Time time = new Time();
		time.hh=timeParts[0];
		time.mm=timeParts[1];
		time.ss=timeParts[2];
		BigDecimal seconds = new BigDecimal(time.hh).multiply(new BigDecimal(60l*60l))
				.add(new BigDecimal(time.mm).multiply(new BigDecimal(60l)))
				.add(new BigDecimal(time.ss));

		if(seconds.compareTo(new BigDecimal(3l*60l)) > 0) {
			item.cost = new BigDecimal(3l*60l).multiply(INITIAL_COST)
					.add(seconds.subtract(new BigDecimal(3l*60l)).multiply(AFTER_3_MIN_COST));

		}else {
			item.cost = seconds.multiply(INITIAL_COST);
		}
	}
	class Time{
		String hh;
		String mm;
		String ss;
	}
	class PhoneRecord{
		String name;
		String number;
		String time;
		BigDecimal cost;
	}
	class NameTotalRecord{
		String name;
		BigDecimal cost;
	}
	class MyHashMap extends HashMap<String, BigDecimal>{
		public BigDecimal put(String key, BigDecimal bd) {
			if(super.get(key) != null) {
				BigDecimal value = bd.add((BigDecimal) super.get(key));
				return super.put(key, value);
			}else {
				return super.put(key, bd);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		PhoneParser pp = new PhoneParser();
		pp.parseFile("res\\calls.log");
		for (PhoneRecord item:pp.phoneItems) {
			pp.calculateInitialCost(item);
		}
		HashSet<String> names = new HashSet();
		for (PhoneRecord item:pp.phoneItems) {
			pp.nameNumberCost.put(item.name+","+item.number,item.cost);
			names.add(item.name);
		}
		for (String name: names) {
			ArrayList<NameTotalRecord> totals = new ArrayList();
			BigDecimal runningTotal = new BigDecimal("0");
			BigDecimal maximum = new BigDecimal("0");
			for (String nameNumber: pp.nameNumberCost.keySet()) {
				if (nameNumber.split(",")[0].equals(name)) {
					runningTotal = pp.nameNumberCost.get(nameNumber).add(runningTotal);
					maximum = (maximum.compareTo(pp.nameNumberCost.get(nameNumber))>0)? maximum : pp.nameNumberCost.get(nameNumber);
					System.out.println(nameNumber+"..."+": "+pp.nameNumberCost.get(nameNumber));
				}
			}
			NameTotalRecord ntr = pp.new NameTotalRecord();
			ntr.name = name;
			ntr.cost = runningTotal.subtract(maximum);
			totals.add(ntr);
			System.out.println(name+"...Total: "+ntr.cost);

		}
	
		
	}

}
