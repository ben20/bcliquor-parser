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

/**
 * This is a script that processes a CVS file containing the location of private
 * liquor stores found from http://www.data.gov.bc.ca/ and uses the Google's
 * GeoCode API to retrieve the latitude and longitude of each location and
 * places it in a new textfile.
 * 
 * This script was intended to be used to create a usable data format in an
 * android app called BC Booze Finder.
 * 
 * @author benkeung
 * 
 */
public class AddressToLatLng {

	private static final String GEOCODE_URL_PREFIX = "http://maps.googleapis.com/maps/api/geocode/json?address=";
	private static final String LIQUOR_STORE_FILE = "/Users/benkeung/Documents/workspace/JavaHackProject/assets/web_lrs.csv";
	private static final String NEW_LATLNG_FILE = "assets/ls-latlng.txt";

	public static void main(String[] args) {

		createFile(NEW_LATLNG_FILE);
		parseCVS(LIQUOR_STORE_FILE);
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
	 * Parses the CVS file and calls the function to create a URL that is
	 * GeoCode API compatible.
	 * 
	 * @param file
	 *            is the filename and path of DataBC's liquor store data
	 */
	private static void parseCVS(String file) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";

			String dataLine[];
			while ((line = br.readLine()) != null) {
				dataLine = line.split(",");

				String city = dataLine[0];
				String address = dataLine[1];
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

				}
				createGeocodeURL(address, city);
			}
		} catch (IOException e) {

		}
	}

	/**
	 * Creates an acceptable GeoCode API URl
	 * 
	 * @return A URL sequence for the GeoCode API
	 */
	private static String createGeocodeURL(String address, String city) {
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
			if (i == 0)
				sb.append(",+" + address_split[i]);
			else
				sb.append("+" + address_split[i]);
		}

		sb.append(",+CA&sensor=true");

		System.out.println(sb.toString());
		addressToLatLng(address, city, sb.toString());

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
			String url_name) {

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
			parseJSONInput(address, city, sb.toString());

			br.close();
		} catch (MalformedURLException e) {

		} catch (IOException e) {

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
	 */
	private static void parseJSONInput(String address, String city, String json) {
		Gson gson = new Gson();

		LiquorStoreLocation ls_location = null;

		ls_location = gson.fromJson(json, LiquorStoreLocation.class);
		List<LiquorStore> liquorStore = ls_location.getResults();

		StringBuilder sb;
		if (liquorStore.isEmpty()) {
			writeToFile(NEW_LATLNG_FILE, "Empty\n");
		} else {
			LiquorStore ls = liquorStore.get(0);

			sb = new StringBuilder();
			sb.append(address + " " + city + "\t");
			String lat = ls.getGeometry().getLocation().getLat() + "\t";
			String lng = ls.getGeometry().getLocation().getLng() + "\n";

			System.out.println(address + lat + lng);

			sb.append(lat);
			sb.append(lng);

			writeToFile(NEW_LATLNG_FILE, sb.toString());
		}
	}
}
