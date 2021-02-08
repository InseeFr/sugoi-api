<?xml version="1.0" encoding="UTF-8"?>
<Courriers>
	<IdOperation></IdOperation> <!-- Nom de l'opération donné par l'équipe éditique pour le pilotage de l'édition -->
	<TypeGenerateur></TypeGenerateur> <!-- Plusieurs valeurs possibles : courrier_fo (modèle xsl-fo sans questionnaire) / questionnaire (modèle xsl-fo avec questionnaire) -->
	<PartieNomFichierLibreZip></PartieNomFichierLibreZip> <!-- Nom du fichier zip en sortie (à définir avec l'équipe éditique)
																 Formats : [IDSOURCE]_[MILLESIME]_[TYPECOURRIER] ou [IDSOURCE]_[MILLESIME]_[IDPERIODE]_[TYPECOURRIER] -->
	<Courrier>
		<Variables>

		<!-- #####################################
			 # Variables imposées par l'éditique #
			 ##################################### -->
			<NumeroDocument>00000001</NumeroDocument> <!-- Balise obligatoire et à renseigner : sur 8 caractères et à incrémenter pour chaque courrier présent dans le xml. Numérotation séquentielle (de 1 à N) propre à chaque fichier xml -->
			<BddIdentifiantUniteEnquetee>433737996000  </BddIdentifiantUniteEnquetee> <!-- Balise obligatoire et à renseigner : identifiant de l'enquêté sur 14 caractères avec éventuellement des blancs pour compléter -->
			<CodePostalDestinataire>33500</CodePostalDestinataire> <!-- Balise obligatoire et à renseigner : sur 5 caractères -->

			<!-- BddAdressePosteeL? : - balises obligatoires et à renseigner en MAJUSCULE pour une meilleure prise en compte par La Poste mais elles peuvent être renseignées à vide si envoi par colis
									  - chaque ligne doit comporter 38 caractères maximum (espaces inclus) -->
			<BddAdressePosteeL1>${address.ligne1}</BddAdressePosteeL1> <!-- Identité du destinataire ou raison sociale - dénomination sociale -->
			<BddAdressePosteeL2>${address.ligne2}</BddAdressePosteeL2> <!-- Identification du point de remise ou identité du destinataire - service -->
			<BddAdressePosteeL3>${address.ligne3}</BddAdressePosteeL3> <!-- Complément de localisation de la construction -->
			<BddAdressePosteeL4>${address.ligne4}</BddAdressePosteeL4> <!-- N° et libéllé de la voie -->
			<BddAdressePosteeL5>${address.ligne5}</BddAdressePosteeL5> <!-- Service de distribution - complément de localisation voie-->
			<BddAdressePosteeL6>${address.ligne6}</BddAdressePosteeL6> <!-- Code postal et localité ou code cedex et libellé cedex-->
			<BddAdressePosteeL7>${address.ligne7}</BddAdressePosteeL7> <!-- Pays -->

			<!-- AdresseRetourL? : - balises obligatoires et à renseigner en MAJUSCULE mais elles peuvent être renseignées à vide si pas de retour attendu d'accusé de réception
								   - chaque ligne doit comporter 38 caractères maximum (espaces inclus) -->
			<AdresseRetourL1></AdresseRetourL1> 
			<AdresseRetourL2></AdresseRetourL2>
			<AdresseRetourL3></AdresseRetourL3>
			<AdresseRetourL4></AdresseRetourL4>
			<AdresseRetourL5></AdresseRetourL5>
			<AdresseRetourL6></AdresseRetourL6>
			<AdresseRetourL7></AdresseRetourL7>
			
			<Barcode>IS332l4000000001433737996000</Barcode> <!-- Balise obligatoire si envoi par courrier / non obligatoire si envoi par colis : utilisée uniquement pour la gestion des PND (Plis Non Distribuables)
																		Pour la renseigner correctement, il faut se référer à la note qui indique comment constituer le Barcode (sur 35 caractères) -->
			
		<!-- ##################################
			 # Variables concernant le modèle #
			 ################################## -->
			<!-- ATTENTION : les noms des variables ne doivent pas comporter d'espace, ni de . ou de - réservés à un usage particulier (par contre le _ est autorisé) -->
			<!-- Longueur maximale des noms de variables : 50 caractères -->
			<!-- Exemples : -->
			<password>${password}</password>

		</Variables>
		<Images></Images> <!-- Balise obligatoire mais elle peut être laissée vide -->
	</Courrier>
</Courriers>
