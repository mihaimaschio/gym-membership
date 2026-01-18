# Documentație Proiect: Gym Management System

## 1. Introducere

### 1.1 Context general
În era digitală actuală, administrarea eficientă a sălilor de fitness necesită soluții software capabile să gestioneze fluxurile complexe de date legate de membri, abonamente și programări. Motivația acestei lucrări rezidă în necesitatea de a oferi o platformă centralizată, sigură și ușor de utilizat atât pentru administratorii sălii, cât și pentru clienți.

Acest proiect își propune să digitalizeze procesele manuale tradiționale, reducând erorile umane și îmbunătățind experiența utilizatorului. Aplicația este construită folosind arhitectura modernă de tip Single Page Application (SPA), separând logica de backend (Java Spring Boot) de interfața de frontend (Vue.js), asigurând astfel scalabilitate și performanță.

### 1.2 Obiective
Principalul obiectiv al proiectului este dezvoltarea unui sistem complet de gestiune pentru o sală de sport "Gym Membership".
Obiectivele specifice includ:
1.  **Gestiunea Utilizatorilor**: Implementarea unui sistem securizat de autentificare și autorizare (JWT) pentru două roluri distincte: Administrator și Membru.
2.  **Managementul Abonamentelor**: Posibilitatea administratorilor de a crea și atribui tipuri de abonamente, și restricționarea membrilor de a-și auto-atribui abonamente fără plată/aprobare.
3.  **Programarea Claselor**: Vizualizarea orarului săptămânal pentru clasele de fitness (ex: Yoga, HIIT).
4.  **Asistent Virtual AI**: Integrarea unui chatbot bazat pe inteligență artificială (Google Gemini) care să ofere sfaturi despre nutriție și antrenament, cu restricții de domeniu stricte.
5.  **Interfață Modernă**: Crearea unui design responsiv și intuitiv pentru utilizatori.

### 1.3 Specificații
**Funcționale:**
*   Sistem de Login/Register securizat.
*   Dashboard dedicat pentru Admin (statistici, management membri) și Membru (status abonament, orar).
*   Chatbot AI interactiv accesibil printr-un buton plutitor ("AI Gym Assistant").
*   Validarea datelor de intrare (email, parole).

**Tehnice:**
*   **Backend**: Java REST API (Spring Boot), securitate via Spring Security & JWT.
*   **Baza de date**: PostgreSQL relațională.
*   **Frontend**: Vue.js 3, Pinia pentru state management, Axios pentru cereri HTTP.
*   **AI**: Integrare cu API-ul Google Gemini (model `gemini-2.5-flash`).

**Limitări:**
*   Aplicația necesită conexiune la internet pentru funcționalitățile AI.
*   Momentan nu include procesare de plăți online (plățile se presupun a fi făcute fizic, adminul activând abonamentul).

## 2. Analiză, proiectare, implementare

### Analiză și Proiectare
Arhitectura aleasă este una stratificată (N-Tier):
1.  **Nivelul de Prezentare (Frontend)**: Realizat în Vue.js. Utilizează componente modulare (`ChatAssistant.vue`, `LoginView.vue`) și un design system bazat pe CSS modern (variabile CSS, flexbox).
2.  **Nivelul de Aplicație (Backend)**: Spring Boot acționează ca un controller REST. Serviciile (`MemberService`, `SubscriptionService`) conțin logica de business.
3.  **Nivelul de Date**: Hibernate/JPA mapează obiectele Java la tabelele din PostgreSQL (`Member`, `Subscription`, `GymClass`).

**Diagrama de Clase (Simplificată):**
*   `Member` (id, email, password, role) - entitatea principală.
*   `Subscription` - tipurile de abonamente disponibile.
*   `MemberSubscription` - tabelă de legătură (many-to-many) ce reține abonamentele active ale membrilor.

### Implementare
**Backend (Java):**
S-a folosit Maven pentru managementul dependențelor.
*   `GeminiService.java`: Gestionează comunicarea cu API-ul extern. Implementează mecanisme de "Retry" (reîncercare) în caz de erori `429 Too Many Requests` și configurează prompt-ul de sistem pentru a limita răspunsurile strict la fitness.
*   `SecurityConfig.java`: Configurează filtrele JWT pentru a proteja rutele sensibile (/admin/**).

**Frontend (Vue.js):**
*   `ChatAssistant.vue`: O componentă complexă ce permite minimizarea ferestrei, randează răspunsurile folosind Markdown (biblioteca `marked`) și menține istoricul conversației.
*   Stilizarea s-a făcut manual în `main.css` pentru a asigura un aspect unic ("premium look"), folosind gradienți și umbre fine, conform cerințelor de design.

**AI Integration:**
S-a implementat un serviciu dedicat care trimite întrebările utilizatorului către modelul Gemini, adăugând instrucțiuni de sistem ("Pretend you are a gym assistant...") pentru a asigura relevanța răspunsurilor.

### Testare și Validare
*   **Testare Manuală**: S-au verificat fluxurile principale: crearea unui cont, imposibilitatea unui membru de a accesa pagina de admin, funcționalitatea chat-ului.
*   **Validare AI**: S-a testat chatbot-ul cu întrebări din afara domeniului (ex: "heads or tails") pentru a verifica dacă refuză răspunsul, conform rutei implementate.
*   **Compatibilitate**: Testat pe Google Chrome și browsere moderne.

## 3. Concluzii

### 3.1 Rezultate obținute
Proiectul a rezultat într-o aplicație funcțională ”Gym Membership System” care îndeplinește toate cerințele inițiale.
Sistemul permite administratorilor o viziune clară asupra membrilor, iar membrilor le oferă un portal modern pentru a-și verifica statusul și a primi sfaturi virtuale. Integrarea AI reprezintă un punct forte, aducând valoare adăugată prin consultanță instantanee 24/7.

### 3.2 Direcții de dezvoltare
1.  **Integrare Plăți**: Adăugarea Stripe/PayPal pentru plata abonamentelor direct din aplicație.
2.  **Mobile App**: Dezvoltarea unei aplicații native (React Native/Flutter) care să folosească același backend.
3.  **Gamification**: Introducerea unui sistem de puncte/badge-uri pentru vizitele la sală.
4.  **Generare Antrenamente**: Extinderea funcționalității AI pentru a genera planuri de antrenament personalizate salvate direct în baza de date a utilizatorului.
