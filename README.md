# Hospital Management System

## Overview

This is a Spring Boot application designed to manage hospital operations, including patient records, doctor information, appointments, and consultations. The system provides a comprehensive solution for healthcare facilities to digitize and streamline their administrative processes. It implements a complete backend with RESTful APIs that can be integrated with any frontend technology.

## Features

The Hospital Management System offers several key features to facilitate hospital operations. It allows for the management of patient records with details such as name, date of birth, and health status. Doctors can be registered in the system with their specialties and contact information. The appointment scheduling feature enables coordination between patients and doctors, with each appointment having a specific status (pending, canceled, or completed). Additionally, the system supports recording consultation details, including date and medical reports.

## Project Structure

The application follows a standard Spring Boot architecture with clear separation of concerns:

### Entity Classes
The core domain objects that represent the data model:

**Patient.java**
```java
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    @Temporal(TemporalType.DATE)
    private Date dateNaissance;
    private boolean malade;
    @OneToMany(mappedBy = "patient",fetch=FetchType.LAZY)
    private Collection<RendezVous> rendezVous;
    private int score;
}
```

**Medecin.java**
```java
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Medecin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String email;
    private String specialite;
    @OneToMany(mappedBy = "medecin",fetch=FetchType.LAZY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Collection<RendezVous> rendezVous;
}
```

**RendezVous.java**
```java
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class RendezVous {
    @Id
    private String id;
    private Date date;
    @Enumerated(EnumType.STRING)
    private StatusRDV status;
    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Patient patient;
    @ManyToOne
    private Medecin medecin;
    @OneToOne(mappedBy = "rendezVous")
    private Consultation consultation;
}
```

**Consultation.java**
```java
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Consultation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date dateConsultation;
    private String rapport;
    @OneToOne
    @JoinColumn(name = "rendez_vous_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private RendezVous rendezVous;
}
```

### Repository Interfaces
Extend JpaRepository to provide data access capabilities:

**PatientRepository.java**
```java
public interface PatientRepository extends JpaRepository<Patient,Long> {
    Patient findByNom(String name);
}
```

**MedecinRepository.java**
```java
public interface MedecinRepository extends JpaRepository<Medecin,Long> {
    Medecin findByNom(String name);
}
```

### Service Layer
Implements business logic:

**IHospitalService.java (Interface)**
```java
public interface IHospitalService {
    Patient savePatient(Patient patient);
    Medecin saveMedecin(Medecin medecin);
    RendezVous saveRDV(RendezVous rendezVous);
    Consultation saveConsultation(Consultation consultation);

    List<Patient> getAllPatients();
    Optional<Patient> getPatientById(Long id);
    Patient findByNom(String nom);
    Patient updatePatient(Patient p);

    void deletePatient(Long id);
}
```

**HospitalServiceImpl.java (Implementation)**
```java
@Service
@Transactional
public class HospitalServiceImpl implements IHospitalService {
    private PatientRepository patientRepository;
    private MedecinRepository medecinRepository;
    private RendezVousRepository rendezVousRepository;
    private ConsultationRepository consultationRepository;

    // Constructor injection
    public HospitalServiceImpl(PatientRepository patientRepository, 
                              MedecinRepository medecinRepository,
                              RendezVousRepository rendezVousRepository, 
                              ConsultationRepository consultationRepository) {
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.consultationRepository = consultationRepository;
    }

    @Override
    public RendezVous saveRDV(RendezVous rendezVous) {
        rendezVous.setId(UUID.randomUUID().toString());
        return rendezVousRepository.save(rendezVous);
    }
    
    // Other service methods implementation...
}
```

### Controller Classes
Expose RESTful APIs:

**PatientRestController.java**
```java
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
    public Patient add(@RequestParam String nom, 
                      @RequestParam boolean malade, 
                      @RequestParam int score) {
        Patient p = new Patient();
        p.setNom(nom);
        if (p.getDateNaissance() == null) {
            p.setDateNaissance(new Date());
        }
        p.setMalade(malade);
        p.setScore(score);
        return hospitalService.savePatient(p);
    }
    
    // Other controller methods...
}
```

## Technical Stack

- Java
- Spring Boot
- Spring Data JPA
- MySQL Database
- RESTful API
- Maven (build tool)
- Lombok (reduces boilerplate code)

## Setup Instructions

1. Ensure you have Java 17+ and Maven installed on your system.
2. Configure MySQL database (port 3308) with username 'root' and no password, or update the application.properties file with your database credentials.
3. Clone the repository to your local machine.
4. Navigate to the project directory and run `mvn clean install` to build the project.
5. Start the application using `mvn spring-boot:run` or by executing the generated JAR file.
6. The application will be accessible at http://localhost:8080.

## API Endpoints

The system provides several REST endpoints for managing hospital data:

### Patient Management
- `GET /patients` - Retrieve all patients
- `GET /patient/{id}` - Get a specific patient by ID
- `POST /addPatient` - Add a new patient
  ```
  Parameters:
  - nom: String (patient name)
  - malade: boolean (illness status)
  - score: int (health score)
  ```
- `GET /searchPatient` - Search for a patient by name
  ```
  Parameters:
  - nom: String (patient name to search)
  ```
- `PUT /updatePatient/{id}` - Update patient information
  ```
  Parameters:
  - nom: String (updated name)
  - malade: boolean (updated illness status)
  - score: int (updated health score)
  ```
- `DELETE /delete/{id}` - Remove a patient from the system

## Database Configuration

The application is configured to connect to a MySQL database named HOSPITAL_DB. If the database does not exist, it will be created automatically. The application uses Hibernate to manage the database schema, with the property spring.jpa.hibernate.ddl-auto set to "create" to generate the schema on startup.

**application.properties**
```properties
spring.application.name=hospital
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3308/HOSPITAL_DB?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.show-sql=true
```

## Development

The project includes sample data initialization in the HospitalApplication class, which creates test patients, doctors, appointments, and consultations when the application starts. This feature is useful for development and testing purposes.

**Sample Data Initialization**
```java
@Bean
CommandLineRunner start(IHospitalService hospitalService, 
                       PatientRepository patientRepository,
                       MedecinRepository medecinRepository, 
                       RendezVousRepository rendezVousRepository,
                       ConsultationRepository consultationRepository) {
    return args -> {
        // Create sample patients
        Stream.of("Mohamed", "Hassan", "Najat")
                .forEach(name -> {
                    Patient patient = new Patient();
                    patient.setNom(name);
                    patient.setDateNaissance(new Date());
                    patient.setMalade(false);
                    hospitalService.savePatient(patient);
                });
        
        // Create sample doctors
        Stream.of("Yassmine", "Amin", "Nour")
                .forEach(name -> {
                    Medecin medecin = new Medecin();
                    medecin.setNom(name);
                    medecin.setEmail(name+"_medecin@emailcom");
                    medecin.setSpecialite(Math.random()>0.5?"Cardio":"Dentiste");
                    hospitalService.saveMedecin(medecin);
                });

        // Create a sample appointment
        Patient patient = patientRepository.findById(1L).orElse(null);
        Medecin medecin = medecinRepository.findByNom("Yassmine");
        
        RendezVous rendezVous = new RendezVous();
        rendezVous.setDate(new Date());
        rendezVous.setStatus(StatusRDV.PENDING);
        rendezVous.setMedecin(medecin);
        rendezVous.setPatient(patient);
        
        RendezVous savedRDV = hospitalService.saveRDV(rendezVous);
        
        // Create a sample consultation
        Consultation consultation = new Consultation();
        consultation.setDateConsultation(new Date());
        consultation.setRendezVous(savedRDV);
        consultation.setRapport("Rapport de la consultation...");
        hospitalService.saveConsultation(consultation);
    };
}
```

## Entity Relationships

The system implements the following entity relationships:

1. **Patient to RendezVous**: One-to-Many (One patient can have multiple appointments)
2. **Medecin to RendezVous**: One-to-Many (One doctor can have multiple appointments)
3. **RendezVous to Consultation**: One-to-One (Each appointment can have one consultation)

These relationships are managed through JPA annotations in the entity classes, ensuring data integrity and proper object mapping.

## resultat

