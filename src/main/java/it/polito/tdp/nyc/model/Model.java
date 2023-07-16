package it.polito.tdp.nyc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.nyc.db.NYCDao;

public class Model {
	
	private List<String> boroughs;
	private List<NTA> NTAs;
	private NYCDao dao;
	
	public Model() {
		this.dao=new NYCDao();
	}
	
	private Graph <NTA, DefaultWeightedEdge> grafo;
	
	
	//questo metodo mi serve da richiamare nel controller per popolare la tendina
	
	public List<String> getBoroughs(){
		if(this.boroughs==null) {
		
			this.boroughs=this.dao.getAllBoroughs();
		}
		return boroughs;
	}
	
	
	
	public void creaGrafo(String borough) {
		this.dao=new NYCDao();
		this.NTAs=this.dao.getNTAByBorough(borough); //facendo cosi ogni grafo ha subito la lsita dei suoi borough, ovvero la lista dei vertici
		
		this.grafo=new SimpleWeightedGraph<NTA, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		Graphs.addAllVertices(this.grafo, NTAs);
		
		//a questo punto ho i vertici, devo settare il peso dell eventuale arco in base al numero di SSIDs distinti
		//IN PRATICA IL PESO E' L UNIONE DEGLI INSIEMI DEI DISTINTI ssid DEI DUE VERTICI
		
		
		//scorro gli tutte le coppie di NTA e faccio un set unico che unisce gli SSIDs di entrambi
		
		for(NTA n1: this.NTAs) {
			for(NTA n2: this.NTAs) {
				
				if(n1.getNTAcode().compareTo(n2.getNTAcode())<0) {//quindi se i codici non sono uguali, uso il compareTO perchè i codici sono stringhe
					Set <String> unione=new HashSet<String> (n1.getSSIDs());
					unione.addAll(n2.getSSIDs()); //cosi ho entrambi i set
					
					Graphs.addEdge(this.grafo, n1, n2, unione.size()); //perche il peso è il numero di  SSID distinti, quindi la lunghezza del set
					
				}
			}
		}
	}
	
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	//ATTENZIONE: il testo vuole ottenere una lista di archi che hanno peso maggiore al peso medio di tutti gli archi del grafo
	//per calcolare la media devo scorrere tutta la lista degli archi e sommarne il peso, fuori dal ciclo lo divido per la dimensione dll'insieme degli archi
	
	public List<Arco> analisiArchi(){
		//calcolo la media, faccio una lista dove metto solo quelli che hanno peso superiore alla media
		//setta sempre prima la media al valore di default
		double media=0.0;
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			media=media+this.grafo.getEdgeWeight(e);      // se conosco l'arco questo è un metodo per prendere il peso corrispondente
		}
		media=media/this.grafo.edgeSet().size();
		
		List<Arco> result=new ArrayList<Arco>();  //dove metto tutti gli archi con peso superiore alla media 
		
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)>media) {
				//quando aggiungo il nuovo arco, chiedo direttamente al grafo di darmi entrambi i vertici ed il peso
				result.add(new Arco(this.grafo.getEdgeSource(e).getNTAcode(), this.grafo.getEdgeTarget(e).getNTAcode(), (int)(this.grafo.getEdgeWeight(e))));
				
			}
		}
		
		Collections.sort(result); //ho messo un compareTo proprio della classe Arco che al momento di comaprare li ordina in ordine decrescente di peso
		return result; 
	}
}


