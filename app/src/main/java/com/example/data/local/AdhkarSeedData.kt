package com.example.data.local

enum class DhikrCategory {
    MORNING,
    EVENING,
    POST_PRAYER
}

data class DhikrItem(
    val id: String,
    val category: DhikrCategory,
    val titleArabic: String,
    val titleEnglish: String,
    val textArabic: String,
    val translationEnglish: String,
    val sourceArabic: String,
    val sourceEnglish: String,
    val targetCount: Int,
    val linkedVerseKeys: List<String> = emptyList()
)

object AdhkarSeedData {

    val defaultTasbihPresets = listOf(
        TasbihSessionEntity("tasbih_1", "سُبْحَانَ اللَّهِ", "SubhanAllah (Glory be to Allah)", 0, 0, 33),
        TasbihSessionEntity("tasbih_2", "الْحَمْدُ لِلَّهِ", "Alhamdulillah (All praise is due to Allah)", 0, 0, 33),
        TasbihSessionEntity("tasbih_3", "اللَّهُ أَكْبَرُ", "Allahu Akbar (Allah is the Greatest)", 0, 0, 33),
        TasbihSessionEntity("tasbih_4", "لَا إِلَٰهَ إِلَّا اللَّهُ", "La ilaha illallah", 0, 0, 100),
        TasbihSessionEntity("tasbih_5", "أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah (I seek forgiveness from Allah)", 0, 0, 100),
        TasbihSessionEntity("tasbih_6", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", "Allahumma salli 'ala Muhammad", 0, 0, 100)
    )

    val allAdhkar: List<DhikrItem> = listOf(
        // Morning Adhkar
        DhikrItem(
            id = "m_sayyid",
            category = DhikrCategory.MORNING,
            titleArabic = "سيد الاستغفار",
            titleEnglish = "Master Supplication for Forgiveness",
            textArabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            translationEnglish = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant, and I abide to Your covenant and promise as best I can...",
            sourceArabic = "رواه البخاري (٦٣٠٦)",
            sourceEnglish = "Sahih al-Bukhari (6306)",
            targetCount = 1
        ),
        DhikrItem(
            id = "m_asbahna",
            category = DhikrCategory.MORNING,
            titleArabic = "ذكر الصباح الجامع",
            titleEnglish = "Morning Praise & Protection",
            textArabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            translationEnglish = "We have reached the morning and at this very time all sovereignty belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah alone...",
            sourceArabic = "رواه مسلم (٢٧٢٣)",
            sourceEnglish = "Sahih Muslim (2723)",
            targetCount = 1
        ),
        DhikrItem(
            id = "m_bismillah",
            category = DhikrCategory.MORNING,
            titleArabic = "دعاء الحفظ في الصباح",
            titleEnglish = "Seeking Protection in Allah's Name",
            textArabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            translationEnglish = "In the name of Allah with whose name nothing is harmed on earth nor in the heavens, and He is The All-Seeing, The All-Knowing.",
            sourceArabic = "رواه أبو داود (٥٠٨٨) والترمذي (٣٣٨٨)",
            sourceEnglish = "Abu Dawud (5088) & Al-Tirmidhi (3388)",
            targetCount = 3
        ),
        DhikrItem(
            id = "m_raditu",
            category = DhikrCategory.MORNING,
            titleArabic = "الرضا بالله رباً",
            titleEnglish = "Contentment with Allah, Islam & Muhammad",
            textArabic = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            translationEnglish = "I am pleased with Allah as a Lord, and Islam as a religion, and Muhammad (peace be upon him) as a Prophet.",
            sourceArabic = "رواه أبو داود (٥٠٧٢) وأحمد (١٨٩٦٧)",
            sourceEnglish = "Abu Dawud (5072) & Ahmad (18967)",
            targetCount = 3
        ),
        DhikrItem(
            id = "m_tasbih",
            category = DhikrCategory.MORNING,
            titleArabic = "سبحان الله وبحمده",
            titleEnglish = "Glorifying Allah",
            textArabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
            translationEnglish = "Glory is to Allah and praise is to Him, by the multitude of His creation, by His pleasure, by the weight of His Throne, and by the extent of His words.",
            sourceArabic = "رواه مسلم (٢٧٢٦)",
            sourceEnglish = "Sahih Muslim (2726)",
            targetCount = 3
        ),
        DhikrItem(
            id = "m_kursi",
            category = DhikrCategory.MORNING,
            titleArabic = "آية الكرسي (البقرة: ٢٥٥)",
            titleEnglish = "Ayat al-Kursi (Al-Baqarah 2:255)",
            textArabic = "تُقرأ آية الكرسي من المصحف الموثق — اضغط لعرض النص القرآني المحقق أو انتقل إلى سورة البقرة الآية ٢٥٥.",
            translationEnglish = "Recite Ayat al-Kursi (2:255) from the verified Mushaf text.",
            sourceArabic = "النسائي في عمل اليوم والليلة (٩٩٢) وصححه الألباني",
            sourceEnglish = "Al-Nasa'i & authenticated in Hisn al-Muslim",
            targetCount = 1,
            linkedVerseKeys = listOf("2:255")
        ),

        // Evening Adhkar
        DhikrItem(
            id = "e_amsayna",
            category = DhikrCategory.EVENING,
            titleArabic = "ذكر المساء الجامع",
            titleEnglish = "Evening Praise & Protection",
            textArabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            translationEnglish = "We have reached the evening and at this very time all sovereignty belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah alone.",
            sourceArabic = "رواه مسلم (٢٧٢٣)",
            sourceEnglish = "Sahih Muslim (2723)",
            targetCount = 1
        ),
        DhikrItem(
            id = "e_kalimat",
            category = DhikrCategory.EVENING,
            titleArabic = "الاستعاذة بكلمات الله التامات",
            titleEnglish = "Seeking Refuge in Allah's Perfect Words",
            textArabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            translationEnglish = "I seek refuge in the Perfect Words of Allah from the evil of what He has created.",
            sourceArabic = "رواه مسلم (٢٧٠٩)",
            sourceEnglish = "Sahih Muslim (2709)",
            targetCount = 3
        ),
        DhikrItem(
            id = "e_afiyah",
            category = DhikrCategory.EVENING,
            titleArabic = "دعاء العافية في الدنيا والآخرة",
            titleEnglish = "Asking for Well-Being",
            textArabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي",
            translationEnglish = "O Allah, I ask You for pardon and well-being in this life and the next. O Allah, I ask You for pardon and well-being in my religion, my worldly affairs, my family and my wealth.",
            sourceArabic = "رواه أبو داود (٥٠٧٤) وابن ماجه (٣٨٧١)",
            sourceEnglish = "Abu Dawud (5074) & Ibn Majah (3871)",
            targetCount = 1
        ),
        DhikrItem(
            id = "e_hayy",
            category = DhikrCategory.EVENING,
            titleArabic = "يا حي يا قيوم",
            titleEnglish = "Ya Hayyu Ya Qayyum",
            textArabic = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
            translationEnglish = "O Ever-Living, O Sustainer, in Your mercy I seek relief; rectify for me all of my affairs and do not leave me to myself even for the blink of an eye.",
            sourceArabic = "رواه النسائي في السنن الكبرى (١٠٤٠٥)",
            sourceEnglish = "Al-Nasa'i in al-Sunan al-Kubra (10405)",
            targetCount = 1
        ),

        // Post-Prayer Adhkar
        DhikrItem(
            id = "p_istighfar",
            category = DhikrCategory.POST_PRAYER,
            titleArabic = "الاستغفار بعد السلام",
            titleEnglish = "Seeking Forgiveness After Prayer",
            textArabic = "أَسْتَغْفِرُ اللَّهَ (ثلاثاً)، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            translationEnglish = "I seek forgiveness from Allah (3x). O Allah, You are Peace and from You comes peace. Blessed are You, O Possessor of Majesty and Honor.",
            sourceArabic = "رواه مسلم (٥٩١)",
            sourceEnglish = "Sahih Muslim (591)",
            targetCount = 1
        ),
        DhikrItem(
            id = "p_tasbihat",
            category = DhikrCategory.POST_PRAYER,
            titleArabic = "التسبيح والتحميد والتكبير",
            titleEnglish = "SubhanAllah, Alhamdulillah, Allahu Akbar (33x)",
            textArabic = "سُبْحَانَ اللَّهِ (٣٣)، وَالْحَمْدُ لِلَّهِ (٣٣)، وَاللَّهُ أَكْبَرُ (٣٣)، وَتَمَامُ الْمِائَةِ: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            translationEnglish = "SubhanAllah (33), Alhamdulillah (33), Allahu Akbar (33), and to complete 100: La ilaha illallahu wahdahu la sharika lah...",
            sourceArabic = "رواه مسلم (٥٩٧)",
            sourceEnglish = "Sahih Muslim (597)",
            targetCount = 33
        ),
        DhikrItem(
            id = "p_ianah",
            category = DhikrCategory.POST_PRAYER,
            titleArabic = "دعاء الإعانة على الذكر والشكر",
            titleEnglish = "Help in Remembrance & Gratitude",
            textArabic = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ",
            translationEnglish = "O Allah, help me to remember You, to give thanks to You, and to worship You in the best manner.",
            sourceArabic = "رواه أبو داود (١٥٢٢) والنسائي (١٣٠٣)",
            sourceEnglish = "Abu Dawud (1522) & Al-Nasa'i (1303)",
            targetCount = 1
        )
    )
}
