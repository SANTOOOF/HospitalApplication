package com.fsm.hospital;

import com.fsm.hospital.entities.*;
import com.fsm.hospital.repositories.ConsultationRepository;
import com.fsm.hospital.repositories.MedecinRepository;
import com.fsm.hospital.repositories.RendezVousRepository;
import com.fsm.hospital.service.IHospitalService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import com.fsm.hospital.repositories.PatientRepository;

@SpringBootApplication
public class HospitalApplication {

	public static void main(String[] args) {
		SpringApplication.run(HospitalApplication.class, args);
	}
	@Bean
	CommandLineRunner start(IHospitalService hospitalService,PatientRepository patientRepository,
							MedecinRepository medecinRepository, RendezVousRepository rendezVousRepository,
							ConsultationRepository consultationRepository) {
		return args -> {
			Stream.of("youssef", "zakaria", "achraf")
					.forEach(name -> {
						Patient patient = new Patient();
						patient.setNom(name);
						patient.setDateNaissance(new Date());
						patient.setMalade(false);
						hospitalService.savePatient(patient);
					});
			Stream.of("hanan", "Amin", "hayat")
					.forEach(name -> {
						Medecin medecin = new Medecin();
						medecin.setNom(name);
						medecin.setEmail(name+"_medecin@emailcom");
						medecin.setSpecialite(Math.random()>0.5?"Cardio":"Dentiste");
						hospitalService.saveMedecin(medecin);
					});

			// Recherche d'un patient par ID (retourne null si non trouvé)
			Patient patient = patientRepository.findById(1L).orElse(null);

			// Recherche d'un patient par nom
			Patient patient1 = patientRepository.findByNom("Mohamed");

			// Recherche d'un médecin par nom
			Medecin medecin = medecinRepository.findByNom("Yassmine");

			// Création d'un nouveau rendez-vous
			RendezVous rendezVous = new RendezVous();

			// Configuration du rendez-vous
			rendezVous.setDate(new Date());
			rendezVous.setStatus(StatusRDV.PENDING);
			rendezVous.setMedecin(medecin);
			rendezVous.setPatient(patient);

			// Sauvegarde du rendez-vous
			RendezVous savedRDV = hospitalService.saveRDV(rendezVous);
			System.out.println(savedRDV.getId());

			// Récupération d'un rendez-vous existant
			RendezVous rendezVous1 = rendezVousRepository.findAll().get(0);

			// Création d'une nouvelle consultation
			Consultation consultation = new Consultation();

			// Configuration de la consultation
			consultation.setDateConsultation(new Date());
			consultation.setRendezVous(rendezVous1);
			consultation.setRapport("Rapport de la consultation...");
			hospitalService.saveConsultation(consultation);

		};
	}

}
