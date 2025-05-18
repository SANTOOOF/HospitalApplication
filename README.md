# Système de Gestion Hospitalière

## Aperçu

Cette application Spring Boot est conçue pour gérer les opérations hospitalières, notamment les dossiers des patients, les informations sur les médecins, les rendez-vous et les consultations. Le système offre une solution complète permettant aux établissements de santé de numériser et de rationaliser leurs processus administratifs. Il implémente un backend complet avec des API RESTful qui peuvent être intégrées à n'importe quelle technologie frontend.

## Fonctionnalités

Le Système de Gestion Hospitalière propose plusieurs fonctionnalités clés pour faciliter les opérations hospitalières. Il permet la gestion des dossiers patients avec des détails tels que le nom, la date de naissance et l'état de santé. Les médecins peuvent être enregistrés dans le système avec leurs spécialités et leurs coordonnées. La fonctionnalité de planification des rendez-vous permet la coordination entre les patients et les médecins, chaque rendez-vous ayant un statut spécifique (en attente, annulé ou terminé). De plus, le système prend en charge l'enregistrement des détails de consultation, y compris la date et les rapports médicaux.

## Structure du Projet

L'application suit une architecture Spring Boot standard avec une séparation claire des préoccupations :

### Classes d'Entités
Les objets de domaine principaux qui représentent le modèle de données :

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

### Interfaces de Repositories
Étendent JpaRepository pour fournir des capacités d'accès aux données :

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

### Couche de Service
Implémente la logique métier :

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

**HospitalServiceImpl.java (Implémentation)**
```java
@Service
@Transactional
public class HospitalServiceImpl implements IHospitalService {
    private PatientRepository patientRepository;
    private MedecinRepository medecinRepository;
    private RendezVousRepository rendezVousRepository;
    private ConsultationRepository consultationRepository;

    // Injection par constructeur
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
    
    // Implémentation des autres méthodes de service...
}
```

### Classes de Contrôleurs
Exposent les API RESTful :

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
    
    // Autres méthodes du contrôleur...
}
```

## Stack Technique

- Java
- Spring Boot
- Spring Data JPA
- Base de données MySQL
- API RESTful
- Maven (outil de build)
- Lombok (réduit le code répétitif)

## Instructions d'Installation

1. Assurez-vous d'avoir Java 17+ et Maven installés sur votre système.
2. Configurez la base de données MySQL (port 3308) avec le nom d'utilisateur 'root' et sans mot de passe, ou mettez à jour le fichier application.properties avec vos identifiants de base de données.
3. Clonez le dépôt sur votre machine locale.
4. Naviguez vers le répertoire du projet et exécutez `mvn clean install` pour construire le projet.
5. Démarrez l'application en utilisant `mvn spring-boot:run` ou en exécutant le fichier JAR généré.
6. L'application sera accessible à l'adresse http://localhost:8080.

## Points de Terminaison API

Le système fournit plusieurs points de terminaison REST pour gérer les données hospitalières :

### Gestion des Patients
- `GET /patients` - Récupérer tous les patients
- `GET /patient/{id}` - Obtenir un patient spécifique par ID
- `POST /addPatient` - Ajouter un nouveau patient
  ```
  Paramètres :
  - nom : String (nom du patient)
  - malade : boolean (statut de maladie)
  - score : int (score de santé)
  ```
- `GET /searchPatient` - Rechercher un patient par nom
  ```
  Paramètres :
  - nom : String (nom du patient à rechercher)
  ```
- `PUT /updatePatient/{id}` - Mettre à jour les informations du patient
  ```
  Paramètres :
  - nom : String (nom mis à jour)
  - malade : boolean (statut de maladie mis à jour)
  - score : int (score de santé mis à jour)
  ```
- `DELETE /delete/{id}` - Supprimer un patient du système

## Configuration de la Base de Données

L'application est configurée pour se connecter à une base de données MySQL nommée HOSPITAL_DB. Si la base de données n'existe pas, elle sera créée automatiquement. L'application utilise Hibernate pour gérer le schéma de la base de données, avec la propriété spring.jpa.hibernate.ddl-auto définie sur "create" pour générer le schéma au démarrage.

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

## Développement

Le projet inclut l'initialisation de données d'exemple dans la classe HospitalApplication, qui crée des patients, des médecins, des rendez-vous et des consultations de test au démarrage de l'application. Cette fonctionnalité est utile à des fins de développement et de test.

**Initialisation des Données d'Exemple**
```java
@Bean
CommandLineRunner start(IHospitalService hospitalService, 
                       PatientRepository patientRepository,
                       MedecinRepository medecinRepository, 
                       RendezVousRepository rendezVousRepository,
                       ConsultationRepository consultationRepository) {
    return args -> {
        // Créer des patients d'exemple
        Stream.of("Mohamed", "Hassan", "Najat")
                .forEach(name -> {
                    Patient patient = new Patient();
                    patient.setNom(name);
                    patient.setDateNaissance(new Date());
                    patient.setMalade(false);
                    hospitalService.savePatient(patient);
                });
        
        // Créer des médecins d'exemple
        Stream.of("Yassmine", "Amin", "Nour")
                .forEach(name -> {
                    Medecin medecin = new Medecin();
                    medecin.setNom(name);
                    medecin.setEmail(name+"_medecin@emailcom");
                    medecin.setSpecialite(Math.random()>0.5?"Cardio":"Dentiste");
                    hospitalService.saveMedecin(medecin);
                });

        // Créer un rendez-vous d'exemple
        Patient patient = patientRepository.findById(1L).orElse(null);
        Medecin medecin = medecinRepository.findByNom("Yassmine");
        
        RendezVous rendezVous = new RendezVous();
        rendezVous.setDate(new Date());
        rendezVous.setStatus(StatusRDV.PENDING);
        rendezVous.setMedecin(medecin);
        rendezVous.setPatient(patient);
        
        RendezVous savedRDV = hospitalService.saveRDV(rendezVous);
        
        // Créer une consultation d'exemple
        Consultation consultation = new Consultation();
        consultation.setDateConsultation(new Date());
        consultation.setRendezVous(savedRDV);
        consultation.setRapport("Rapport de la consultation...");
        hospitalService.saveConsultation(consultation);
    };
}
```

## Relations entre Entités

Le système implémente les relations d'entités suivantes :

1. **Patient à RendezVous** : One-to-Many (Un patient peut avoir plusieurs rendez-vous)
2. **Medecin à RendezVous** : One-to-Many (Un médecin peut avoir plusieurs rendez-vous)
3. **RendezVous à Consultation** : One-to-One (Chaque rendez-vous peut avoir une consultation)

Ces relations sont gérées par des annotations JPA dans les classes d'entités, assurant l'intégrité des données et un mappage d'objets approprié.

## Resultat

![Screenshot 2025-05-18 012353](https://github.com/user-attachments/assets/c0681ebe-69de-4ff4-88ce-e12d9eeafd48)

![Screenshot 2025-05-18 160146](https://github.com/user-attachments/assets/02543a5a-6c5c-4949-9175-ac0e6d4fa072)

![Screenshot 2025-05-18 160245](https://github.com/user-attachments/assets/bb99ba96-4f4f-4423-863c-7402854445c8)

## LES REQUETTE AVEC POSTMAN


