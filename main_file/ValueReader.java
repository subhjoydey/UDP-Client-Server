package udpSend.Receive;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ValueReader {

	public  List<Coordinates> arrCoordinates = new ArrayList<Coordinates>();
	//list of coordinates from the file

	public ValueReader(BufferedReader read){
		
		BufferedReader reader;
		//object of buffered reader class
		
		try {
			reader=read;
			String line = reader.readLine();
			if (line != null)
				getCoordinatesFromLine(line);
			while (line != null) {
				// read next line
				line = reader.readLine();
				if (line != null)
					getCoordinatesFromLine(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private  void getCoordinatesFromLine(String line) {

		Coordinates coordinates = new Coordinates();
		coordinates.setX(Float.parseFloat(line.substring(1, 
									line.indexOf(",")).trim()));
		coordinates.setY(Float.parseFloat(line.substring(line.indexOf(",")
								+ 1, line.indexOf(")")).trim()));
		//extracting coordinates and storing as coordinates
		
		arrCoordinates.add(coordinates);

	}
}
