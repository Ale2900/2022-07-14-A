package it.polito.tdp.nyc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.polito.tdp.nyc.model.Hotspot;
import it.polito.tdp.nyc.model.NTA;

public class NYCDao {
	
	public List<Hotspot> getAllHotspot(){
		String sql = "SELECT * FROM nyc_wifi_hotspot_locations";
		List<Hotspot> result = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Hotspot(res.getInt("OBJECTID"), res.getString("Borough"),
						res.getString("Type"), res.getString("Provider"), res.getString("Name"),
						res.getString("Location"),res.getDouble("Latitude"),res.getDouble("Longitude"),
						res.getString("Location_T"),res.getString("City"),res.getString("SSID"),
						res.getString("SourceID"),res.getInt("BoroCode"),res.getString("BoroName"),
						res.getString("NTACode"), res.getString("NTAName"), res.getInt("Postcode")));
			}
			
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}

		return result;
	}
	
	//query che mi serve per popolare la prima tendina da cui prendere  l'input del grafo
	public List<String> getAllBoroughs(){
		String sql="SELECT DISTINCT Borough "
				+ "FROM nyc_wifi_hotspot_locations "
				+ "ORDER BY Borough";
		List<String> result=new ArrayList<String>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			while(res.next()) {
				result.add(res.getString("Borough"));
			}
			conn.close();
			return result;
			
			
		}catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}
		
		
		
	}
	
	
	//QUERY PER I VERTICI
	//ATTENZIONE: ogni borough ha un set di SSID, percio devi stare attenta all' aggiunta del set nel costruttore di NTA dalla result della query
	public List<NTA> getNTAByBorough(String borough){
		
		String sql="SELECT DISTINCT NTACode, SSID "     //il DISTINCT vale per gli SSID, cosi che possa ottenere tutta la lista per ogni distinto SSID
				+ "FROM nyc_wifi_hotspot_locations "
				+ "WHERE Borough=?"
				+ "ORDER BY NTACode";
		
		//ATTENZIONE: qui mi rendo conto che posso fare sia la query degli archi che quella dei vertici contemporaneamente
		//infatti mentre i vertici mi richiedono gli NTA, gli archi mi richiedono gli SSID, posso percio fare tutto con una sola query
		
		//NTA è una classe che ho creato io per ottenere tutte le informazione per vertici e archi da una sola query al DB
		List<NTA> result=new ArrayList<NTA> ();
		
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, borough);
			ResultSet res = st.executeQuery();
			
			String lastNTACode="";
			while(res.next()) {
	
				//controllo prima che l'elemento risusltante dela query non sia una stringa vuota
				if(!res.getString("NTACode").equals(lastNTACode)) {
					Set<String>ssids=new HashSet<String>();
					ssids.add(res.getString("SSID"));
					
					result.add(new NTA(res.getString("NTACode"), ssids));
					lastNTACode=res.getString("NTACode");
				} else {
					result.get(result.size()+-1).getSSIDs().add(res.getString("SSID"));
				}

			}
			conn.close();
			return result;
			
		}catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}
		
	}
	
	
}
