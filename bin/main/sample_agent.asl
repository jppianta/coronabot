+request(Req)
	:true
<-
	.print("Recebido request ",Req," do Dialog");
	!responder(Req);
	.
	
+!responder(Req)
	: (Req == "febre")
<-
	addSintoma("febre");
	.
+!responder(Req)
	: (Req == "cansaco")
<-
	addSintoma("cansaço");
	.
+!responder(Req)
	: (Req == "tosse seca")
<-
	addSintoma("tosse seca");
	.
+!responder(Req)
	: (Req == "fim de sintomas") & risco(nulo)
<-
	reply_sintomas("nulo")
	.
+!responder(Req)
	: (Req == "fim de sintomas") & risco(baixo)
<-
	reply_sintomas("baixo")
	.
+!responder(Req)
	: (Req == "fim de sintomas") & risco(medio)
<-
	reply_sintomas("medio")
	.
+!responder(Req)
	: true
<-
	.print("default")
	.


{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }