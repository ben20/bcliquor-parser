package com.bcboozeparser.main;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the JSON data used in GSON.
 * @author benkeung
 *
 */
public class LiquorStoreLocation {

	List<LiquorStore> results = new ArrayList<LiquorStore>();
	
	public List<LiquorStore> getResults() {
		return results;
	}

	class LiquorStore {

		String formatted_address;
		Geometry geometry;
		
		public String getFormattedAddress() {
			return formatted_address;
		}
		
		public Geometry getGeometry() {
			return geometry;
		}

		class Geometry {
			Location location;

			public Location getLocation() {
				return location;
			}
			
			class Location {

				String lat;
				String lng;

				public String getLat() {
					return lat;
				}
				public String getLng() {
					return lng;
				}
			}
		}
	}
}
