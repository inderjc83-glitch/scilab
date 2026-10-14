package com.example.i18n

import com.example.model.AppLanguage

object StringsLocalization {
    fun get(key: String, lang: AppLanguage): String {
        return translations[key]?.get(lang) ?: translations[key]?.get(AppLanguage.ENGLISH) ?: key
    }

    private val translations = mapOf(
        "app_title" to mapOf(
            AppLanguage.ENGLISH to "SciLab",
            AppLanguage.SPANISH to "SciLab",
            AppLanguage.FRENCH to "SciLab",
            AppLanguage.GERMAN to "SciLab",
            AppLanguage.HINDI to "साइलैब"
        ),
        "experiments" to mapOf(
            AppLanguage.ENGLISH to "Experiments",
            AppLanguage.SPANISH to "Experimentos",
            AppLanguage.FRENCH to "Expériences",
            AppLanguage.GERMAN to "Experimente",
            AppLanguage.HINDI to "प्रयोग"
        ),
        "teacher_dashboard" to mapOf(
            AppLanguage.ENGLISH to "Teacher Portal",
            AppLanguage.SPANISH to "Portal Docente",
            AppLanguage.FRENCH to "Portail Enseignant",
            AppLanguage.GERMAN to "Lehrer-Portal",
            AppLanguage.HINDI to "शिक्षक पोर्टल"
        ),
        "quizzes" to mapOf(
            AppLanguage.ENGLISH to "Science Quiz",
            AppLanguage.SPANISH to "Cuestionario",
            AppLanguage.FRENCH to "Quiz Scientifique",
            AppLanguage.GERMAN to "Wissenschafts-Quiz",
            AppLanguage.HINDI to "विज्ञान प्रश्नोत्तरी"
        ),
        "profile" to mapOf(
            AppLanguage.ENGLISH to "Profile & Security",
            AppLanguage.SPANISH to "Perfil y Seguridad",
            AppLanguage.FRENCH to "Profil & Sécurité",
            AppLanguage.GERMAN to "Profil & Sicherheit",
            AppLanguage.HINDI to "प्रोफ़ाइल और सुरक्षा"
        ),
        "realtime_graph" to mapOf(
            AppLanguage.ENGLISH to "Real-Time Telemetry Graph",
            AppLanguage.SPANISH to "Gráfico de Telemetría en Tiempo Real",
            AppLanguage.FRENCH to "Graphique Télémétrique en Temps Réel",
            AppLanguage.GERMAN to "Echtzeit-Telemetrie-Diagramm",
            AppLanguage.HINDI to "रीयल-टाइम टेलीमेट्री ग्राफ"
        ),
        "data_logs" to mapOf(
            AppLanguage.ENGLISH to "Data Logs",
            AppLanguage.SPANISH to "Registros de Datos",
            AppLanguage.FRENCH to "Journaux de Données",
            AppLanguage.GERMAN to "Datenprotokolle",
            AppLanguage.HINDI to "डेटा लॉग"
        ),
        "lab_notes" to mapOf(
            AppLanguage.ENGLISH to "Collaborative Notes",
            AppLanguage.SPANISH to "Notas Colaborativas",
            AppLanguage.FRENCH to "Notes Collaboratives",
            AppLanguage.GERMAN to "Kollaborative Notizen",
            AppLanguage.HINDI to "सहयोगात्मक नोट्स"
        ),
        "team_tasks" to mapOf(
            AppLanguage.ENGLISH to "Project Timeline",
            AppLanguage.SPANISH to "Línea de Tiempo",
            AppLanguage.FRENCH to "Chronologie du Projet",
            AppLanguage.GERMAN to "Projekt-Zeitplan",
            AppLanguage.HINDI to "परियोजना समयरेखा"
        ),
        "chat_module" to mapOf(
            AppLanguage.ENGLISH to "Peer Lab Chat",
            AppLanguage.SPANISH to "Chat de Laboratorio",
            AppLanguage.FRENCH to "Discussion de Labo",
            AppLanguage.GERMAN to "Labor-Chat",
            AppLanguage.HINDI to "सहपाठी चैट"
        ),
        "trial_banner" to mapOf(
            AppLanguage.ENGLISH to "Free Trial: %d days remaining. Unlock all features with premium subscription.",
            AppLanguage.SPANISH to "Prueba gratuita: quedan %d días. Desbloquee funciones con suscripción.",
            AppLanguage.FRENCH to "Essai gratuit : %d jours restants. Débloquez tout avec un abonnement.",
            AppLanguage.GERMAN to "Kostenlose Testversion: Noch %d Tage. Schalten Sie Premium frei.",
            AppLanguage.HINDI to "निःशुल्क परीक्षण: %d दिन शेष। प्रीमियम सदस्यता से सभी सुविधाएँ अनलॉक करें।"
        ),
        "upgrade_now" to mapOf(
            AppLanguage.ENGLISH to "Upgrade Now",
            AppLanguage.SPANISH to "Mejorar Ahora",
            AppLanguage.FRENCH to "Passer Premium",
            AppLanguage.GERMAN to "Jetzt Upgraden",
            AppLanguage.HINDI to "अपग्रेड करें"
        ),
        "e2e_encrypted" to mapOf(
            AppLanguage.ENGLISH to "AES-256 Encrypted",
            AppLanguage.SPANISH to "Cifrado AES-256",
            AppLanguage.FRENCH to "Chiffré AES-256",
            AppLanguage.GERMAN to "AES-256 Verschlüsselt",
            AppLanguage.HINDI to "AES-256 एन्क्रिप्टेड"
        ),
        "offline_field_mode" to mapOf(
            AppLanguage.ENGLISH to "Offline Field Mode",
            AppLanguage.SPANISH to "Modo Campo sin Conexión",
            AppLanguage.FRENCH to "Mode Terrain Hors-Ligne",
            AppLanguage.GERMAN to "Offline-Feldmodus",
            AppLanguage.HINDI to "ऑफ़लाइन फ़ील्ड मोड"
        ),
        "auto_feedback" to mapOf(
            AppLanguage.ENGLISH to "Automated AI Analysis",
            AppLanguage.SPANISH to "Análisis IA Automatizado",
            AppLanguage.FRENCH to "Analyse IA Automatisée",
            AppLanguage.GERMAN to "Automatische KI-Analyse",
            AppLanguage.HINDI to "स्वचालित एआई विश्लेषण"
        )
    )
}
