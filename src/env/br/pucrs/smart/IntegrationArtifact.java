// CArtAgO artifact code for project helloworld_from_jason

package br.pucrs.smart;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.JsonObject;

import br.pucrs.smart.interfaces.IAgent;
import br.pucrs.smart.models.OutputContexts;
import br.pucrs.smart.models.ResponseDialogflow;
import cartago.*;
import jason.asSyntax.Literal;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;

public class IntegrationArtifact extends Artifact implements IAgent {
	private Logger logger = Logger.getLogger("ArtefatoIntegracao." + IntegrationArtifact.class.getName());
	private HashSet<String> sintomas = new HashSet<String>();
	private Term[] risco = {
			ASSyntax.createAtom("nulo"),
			ASSyntax.createAtom("baixo"),
			ASSyntax.createAtom("medio"),
			ASSyntax.createAtom("alto"),
	};
	String jasonResponse = null;

	void init() {
		RestImpl.setListener(this);
		defineObsProperty("risco", this.risco[0]);
	}

	@INTERNAL_OPERATION
	void defineRequest(String obsProperty) {
		defineObsProperty("request", obsProperty);
	}

	@OPERATION
	void reply(String response) {
		this.jasonResponse = response;
	}
	
	@OPERATION
	void reply_sintomas(String risco) {
		String padrao = "mas ainda assim recomendaria a visita de um mÈdico";
		switch(risco) {
			case "nulo": {
				this.jasonResponse = "VocÍ n„o parece ter nenhum sintoma caracterÌstico de covid, " + padrao;
				break;
			}
			case "baixo": {
				this.jasonResponse = "Eu diria que vocÍ tem um risco baixo de covid-19, apresentando apenas " + this.getSintomas() + padrao;
				break;
			}
			case "medio": {
				this.jasonResponse = "VocÍ tem um risco consider·vel de covid-19, apresentando " + this.getSintomas() + "È bastante recomend·vel a visita de um mÈdico";
			}
		}
	}
	
	private String getSintomas() {
		String sint = "";
		for (String sintoma : this.sintomas) {
			sint += sintoma + ", ";
		}
		
		return sint;
	}
	
	@OPERATION
	void addSintoma(String sintoma) {
		this.sintomas.add(sintoma);
		this.updateRisco();
	}
	
	@INTERNAL_OPERATION
	void updateRisco() {
		ObsProperty prop = getObsProperty("risco");
		if (this.sintomas.size() == 1) {
			prop.updateValue(this.risco[1]);
		} else if (this.sintomas.size() >= 2 && this.sintomas.size() <= 4) {
			prop.updateValue(this.risco[2]);
		} else if (this.sintomas.size() > 4) {
			prop.updateValue(this.risco[3]);
		}
	}

	@Override
	public ResponseDialogflow processarIntencao(String sessionId, String request, HashMap<String, String> parameters, List<OutputContexts> outputContexts, String fullfilmentText) {
		ResponseDialogflow response = new ResponseDialogflow();
		System.out.println("recebido evento: " + sessionId);
		System.out.println("IntenÁ„o: " + request);
		if (request != null) {
			for(Map.Entry<String, String> entry : parameters.entrySet()) {
			    String key = entry.getKey();
			    String value = entry.getValue();

				System.out.println("parameters: " + key + " : " + value);

			}
			if (outputContexts != null) {
				for (OutputContexts outputContext : outputContexts) {
					System.out.println("OutputContexts name: " + outputContext.getName());
					System.out.println("OutputContexts lifespanCount: " + outputContext.getLifespanCount());
					System.out.println("OutputContexts parameters: ");
					for(Map.Entry<String, String> entry : parameters.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						System.out.println(key + " : " + value);
					}
				}
			}

			execInternalOp("defineRequest", request);
			System.out.println("Definindo propriedade observ√°vel");
		} else {
			System.out.println("N√£o foi poss√≠vel definir a propriedade observ√°vel");
			response.setFulfillmentText("IntenÁ„o n„o reconhecida");
		}
		int i = 0;
		while (this.jasonResponse == null && i <= 200) {
			try {
				Thread.sleep(10);
				i++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (this.jasonResponse != null) {
			System.out.println("jasonResponse " + this.jasonResponse);
			response.setFulfillmentText(this.jasonResponse);
			this.jasonResponse = null;
		} else {
			System.out.println("Sem jasonResponse");
			if (fullfilmentText != null) {
				response.setFulfillmentText(fullfilmentText);
			} else {
				response.setFulfillmentText("Sem resposta do agente");				
			}
		}
		return response;
	}
	

//	public void simStartMessage(JsonObject starMessage) {
//		List<String> filter = Arrays.asList("id", "map");
//		JsonObject config = starMessage.get("agent_percepts").getAsJsonObject();
//		JsonObject map = starMessage.get("map_percepts").getAsJsonObject();
//		// we need to ensure the token will be an atom
//		String token = config.get("token").getAsString();
//		config.remove("token");
//		config.addProperty("token", "\'"+token+"\'");
//		filter.forEach(f -> map.remove(f));
//
//		try {
//			List<Percept> p = new ArrayList<Percept>();
//			p.addAll(Translator.entryToPercept(config.entrySet()));
//			p.addAll(Translator.entryToPercept(map.entrySet()));
//
//			execInternalOp("updatePerceptions", null, p, null);
//		} catch (ParseException e) {
//			logger.info("failed to parse initial percetions: " + e.getMessage());
//		}
//	}
//	
//	@INTERNAL_OPERATION
//	private void updatePerceptions(Collection<Percept> previousPercepts, Collection<Percept> percepts,
//			List<String> orderPercept) {
//		if (previousPercepts == null) {// should add all new perceptions
//			for (Percept percept : percepts) {
//				try {
//					Literal literal = Translator.perceptToLiteral(percept);
//					defineObsProperty(literal.getFunctor(), (Object[]) literal.getTermsArray());
//				} catch (JasonException e) {
//					logger.info("Failed to parse percept to literal: " + e.getMessage());
//				}
//			}
//		}
//	}

}
