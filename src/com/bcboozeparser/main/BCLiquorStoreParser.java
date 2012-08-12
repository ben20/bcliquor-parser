package com.bcboozeparser.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Formatter;
import java.util.List;

import com.bcboozeparser.main.LiquorStoreLocation.LiquorStore;
import com.google.gson.Gson;

public class BCLiquorStoreParser {

	private static final String GEOCODE_URL_PREFIX = "http://maps.googleapis.com/maps/api/geocode/json?address=";
	private static final String LIQUOR_STORE_FILE = "/Users/benkeung/Documents/workspace/BCBoozeParser/assets/BC_Liquor_Store_Locations-text.txt";
	private static final String NEW_LATLNG_FILE = "assets/ls-latlng_BCL.txt";

	public static void main(String[] arigs) {
		createFile(NEW_LATLNG_FILE);
		parseBCLiquorStoreCVS(LIQUOR_STORE_FILE);
	}

	/**
	 * Creates a file. This file is going to contain the new formatted data
	 * 
	 * @param file_name
	 *            is the filename of the file
	 */
	private static void createFile(String file_name) {

		final Formatter x;

		try {
			x = new Formatter(file_name);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Writes a given line to a file. The line is APPENDED at the END of the
	 * last line in the file.
	 */
	private static void writeToFile(String file_name, String line_to_write) {

		try {
			FileWriter fstream = new FileWriter(file_name, true);
			BufferedWriter br = new BufferedWriter(fstream);
			br.write(line_to_write);
			br.close();
		} catch (Exception e) {
			System.out.println("Exception caught: " + e.getMessage());
		}
	}

	/**
	 * Parses the BC liquor store file [0] - store number [1] - name [2] -
	 * address [3] - city [4] - postal code [5] - phone [6] - fax
	 * 
	 * @param file
	 */
	private static void parseBCLiquorStoreCVS(String file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			String dataLine[];

			while ((line = br.readLine()) != null) {
				dataLine = line.split("\t");

				// System.out.println(dataLine[0] + ": " + dataLine.length);
				String address = dataLine[2];
				String city = dataLine[3];

				// ie. store number, name, phone number, postal code,
				// delimited by \t
				// 0 - store num; 1 - name; 2 - postal code; 3 - phone
				StringBuilder information = new StringBuilder();
				information.append(dataLine[0] + "\t");
				information.append(dataLine[1] + "\t");
				information.append(dataLine[4] + "\t");
				information.append(dataLine[5] + "\t");

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

				}
				createGeocodeURL_BCL(address, city, information.toString());
			}
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
	}

	/**
	 * Creates an acceptable GeoCode API URL.
	 * 
	 * @return A URL sequence for the GeoCode API
	 */
	private static String createGeocodeURL_BCL(String address, String city,
			String information) {
		StringBuilder sb = new StringBuilder();
		sb.append(GEOCODE_URL_PREFIX);

		String city_split[];
		city_split = city.split(" ");

		for (int m = 0; m < city_split.length; m++) {
			if (m == 0)
				sb.append(city_split[m]);
			else
				sb.append("+" + city_split[m]);
		}

		sb.append(",+CANADA+BC");
		String address_split[];
		address_split = address.split(" ");
		for (int i = 0; i < address_split.length; i++) {
			if (!address_split[i].contains("#")
					&& !address_split[i].contains("-")) {

				if (i == 0)
					sb.append(",+" + address_split[i]);

				else
					sb.append("+" + address_split[i]);
			}
		}

		String infoSplit[];
		infoSplit = information.split("\t");
		String postalSplit[];
		postalSplit = infoSplit[2].split(" ");
		sb.append(",+" + postalSplit[0]);
		sb.append("+" + postalSplit[1]);

		sb.append(",+CA&sensor=true");

//		System.out.println(sb.toString());
		 addressToLatLng(address, city, sb.toString(), information);

		return sb.toString();
	}

	/**
	 * Calls the GeoCode API from the given url and parses the corresponding
	 * json response.
	 * 
	 * @param address
	 *            , city are to be passed on to the parseJsonInput() method
	 * 
	 */
	private static void addressToLatLng(String address, String city,
			String url_name, String information) {

		try {
			URL my_url = new URL(url_name);
			URLConnection urlConnection = my_url.openConnection();
			urlConnection.connect();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));

			StringBuilder sb = new StringBuilder();

			String input;

			while ((input = br.readLine()) != null) {
				sb.append(input);
			}

			parseJSONInput(address, city, sb.toString(), information);

			System.out.println(sb.toString());
			br.close();
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Uses GSON to parse the json string return from the GeoCode API call.
	 * 
	 * Stores the original address and city from DataBC's data file in the
	 * parsed text file instead of the address returned in the JSON.
	 * 
	 * CHANGES: - only parses the first result from the JSON output. The
	 * multiple results in the JSON are due to the location being found in a
	 * complex. (I think).
	 * 
	 * 'information' index values: // 0 - store num; 1 - name; 2 - postal code;
	 * 3 - phone
	 */
	private static void parseJSONInput(String address, String city,
			String json, String information) {
		Gson gson = new Gson();

		LiquorStoreLocation ls_location = null;

		ls_location = gson.fromJson(json, LiquorStoreLocation.class);
		List<LiquorStore> liquorStore = ls_location.getResults();

		StringBuilder sb;

		if (liquorStore.isEmpty()) {
			System.out.println("EMPTY");
			sb = new StringBuilder();
			sb.append(address + " " + city + "\t");
			sb.append("LAT\tLNG\t");
			sb.append(information + "\n");

			writeToFile(NEW_LATLNG_FILE, sb.toString());
			// writeToFile(NEW_LATLNG_FILE, "Empty\n");
		} else {
			System.out.println("NOT EMPTY");
			sb = new StringBuilder();

			LiquorStore ls = liquorStore.get(0);

			// information should already contain a final \t
			sb.append(address + "\t" + city + "\t");
			String lat = ls.getGeometry().getLocation().getLat() + "\t";
			String lng = ls.getGeometry().getLocation().getLng() + "\t";

			sb.append(lat);
			sb.append(lng);
			sb.append(information + "\n");

			writeToFile(NEW_LATLNG_FILE, sb.toString());
		}
	}
}
