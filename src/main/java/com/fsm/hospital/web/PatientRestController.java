package com.fsm.hospital.web;

import com.fsm.hospital.entities.Patient;
import com.fsm.hospital.repositories.PatientRepository;
import com.fsm.hospital.service.IHospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@RestController
public class PatientRestController {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private IHospitalService hospitalService;

    

    @GetMapping("/patients")
    public List<Patient> patientList() {
        return patientRepository.findAll();
    }

    @GetMapping("/patient/{id}")
    public Patient getPatient(@PathVariable Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Patient non trouvé"));
    }

    @PostMapping("/addPatient")
    @ResponseStatus(HttpStatus.CREATED)
    public Patient add(@RequestParam String nom, @RequestParam boolean malade, @RequestParam int score) {
        Patient p = new Patient();
        p.setNom(nom);
        if (p.getDateNaissance() == null) {
            p.setDateNaissance(new Date());
        }
        p.setMalade(malade);
        p.setScore(score);
        return hospitalService.savePatient(p);
    }


    @GetMapping("/searchPatient")
    public Patient search(@RequestParam String nom) {
        return hospitalService.findByNom(nom);
    }
    @PutMapping("/updatePatient/{id}")
    public Patient update(@PathVariable Long id, @RequestParam String nom, @RequestParam boolean malade, @RequestParam int score) {

        Patient exist = hospitalService.getPatientById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Patient non trouvé"));
        exist.setNom(nom);
        exist.setMalade(malade);
        exist.setScore(score);
        return hospitalService.updatePatient(exist);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        hospitalService.deletePatient(id);
    }

}