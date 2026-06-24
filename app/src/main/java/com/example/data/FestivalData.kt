package com.example.data

enum class FestivalType(val label: String) {
    NATIONAL("National Public Holiday"),
    GAZETTED("Gazetted Public Holiday"),
    RESTRICTED("Restricted / Optional Holiday"),
    REGIONAL("Regional Festive Occasion")
}

data class Festival(
    val name: String,
    val dateStr: String, // e.g., "Jan 26, 2026 (Monday)"
    val month: Int,    // 1-indexed: 1 = Jan, 12 = Dec
    val day: Int,
    val type: FestivalType,
    val description: String,
    val ritualOrTip: String
) {
    fun getFormattedDate(year: Int): String {
        return try {
            val cal = java.util.GregorianCalendar(year, month - 1, day)
            val formatter = java.text.SimpleDateFormat("MMMM dd, yyyy (EEEE)", java.util.Locale.getDefault())
            formatter.format(cal.time)
        } catch (e: Exception) {
            dateStr
        }
    }
}

object FestivalData {
    val list2026 = listOf(
        // January
        Festival(
            name = "New Year's Day",
            dateStr = "January 01, 2026 (Thursday)",
            month = 1,
            day = 1,
            type = FestivalType.RESTRICTED,
            description = "The first day of the Gregorian calendar year, celebrated globally with fireworks, countdowns, and resolutions.",
            ritualOrTip = "Begin the year with positive affirmations, connect with distant family, and set personal growth goals."
        ),
        Festival(
            name = "Makar Sankranti / Uttarayan",
            dateStr = "January 14, 2026 (Wednesday)",
            month = 1,
            day = 14,
            type = FestivalType.REGIONAL,
            description = "A major harvest festival marking the transition of the Sun into Capricorn (Makara rashi) and the return of longer days.",
            ritualOrTip = "Fly colorful kites, eat sweet sesame laddoos (Til-Gud), and donate food/clothes to those in need."
        ),
        Festival(
            name = "Pongal",
            dateStr = "January 14, 2026 (Wednesday)",
            month = 1,
            day = 14,
            type = FestivalType.REGIONAL,
            description = "A four-day harvested thanksgiving festival celebrated in Tamil Nadu to thank the Sun God and farm cattle.",
            ritualOrTip = "Boil fresh rice with milk and jaggery in a decorated clay pot until it overflows, chanting 'Pongalo Pongal!'"
        ),
        Festival(
            name = "Magh Bihu",
            dateStr = "January 15, 2026 (Thursday)",
            month = 1,
            day = 15,
            type = FestivalType.REGIONAL,
            description = "An Assamese harvest festival signaling the shift of solar cycles. Friends and family construct temporary leaf cottages called Meji.",
            ritualOrTip = "Gather around the night bonfire (Meji), offer prayers to Agni, and feast on traditional Pitha (rice cakes)."
        ),
        Festival(
            name = "Republic Day of India",
            dateStr = "January 26, 2026 (Monday)",
            month = 1,
            day = 26,
            type = FestivalType.NATIONAL,
            description = "National Holiday celebrating the day the Constitution of India came into effect in 1950, transitioning India into an independent republic.",
            ritualOrTip = "Watch the stunning Republic Day Parade from New Delhi, sing the National Anthem, and wear tricolor attire."
        ),

        // February
        Festival(
            name = "Vasant Panchami / Saraswati Puja",
            dateStr = "February 01, 2026 (Sunday)",
            month = 2,
            day = 1,
            type = FestivalType.RESTRICTED,
            description = "A spring-welcoming festival dedicated to Goddess Saraswati, the patron deity of knowledge, music, arts, and wisdom.",
            ritualOrTip = "Wear bright yellow clothes, lay books or musical instruments near Goddess Saraswati, and eat yellow sweet rice."
        ),
        Festival(
            name = "Maha Shivratri",
            dateStr = "February 15, 2026 (Sunday)",
            month = 2,
            day = 15,
            type = FestivalType.GAZETTED,
            description = "A solemn night festival dedicated to Lord Shiva, celebrating the Cosmic Dance (Tandava) and Shiva-Parvati's divine wedding.",
            ritualOrTip = "Observe a full or partial fast, offer Bael leaves, raw milk, and water to the Shiva Lingam, and chant 'Om Namah Shivaya'."
        ),

        // March
        Festival(
            name = "Holi (Festival of Colors)",
            dateStr = "March 03, 2026 (Tuesday)",
            month = 3,
            day = 3,
            type = FestivalType.GAZETTED,
            description = "The historic spring festival expressing the triumph of good over evil, the arrival of spring, and a day to forget old grievances.",
            ritualOrTip = "Play with organic herbal Gulal colors, light the Holika bonfire on Holi Eve, and serve Thandai and hot Gujiyas."
        ),
        Festival(
            name = "Eid al-Fitr (Meethi Eid)",
            dateStr = "March 20, 2026 (Friday)",
            month = 3,
            day = 20,
            type = FestivalType.GAZETTED,
            description = "A joyful Islamic holiday celebrated worldwide that marks the end of Ramadan, the holy month of fasting.",
            ritualOrTip = "Wear new clothes, perform Eid prayers, distribute 'Zakat' (alms to poor), and serve delicious sweet Sheer Khurma."
        ),
        Festival(
            name = "Ugadi / Gudi Padwa",
            dateStr = "March 19, 2026 (Thursday)",
            month = 3,
            day = 19,
            type = FestivalType.RESTRICTED,
            description = "The traditional Lunar New Year celebrated with joy across Maharashtra, Andhra Pradesh, Telangana, and Karnataka.",
            ritualOrTip = "Prace a decorated Gudi flag on your balcony, and consume 'Ugadi Pachadi' representing the 6 flavors of life's experiences."
        ),
        Festival(
            name = "Rama Navami",
            dateStr = "March 27, 2026 (Friday)",
            month = 3,
            day = 27,
            type = FestivalType.RESTRICTED,
            description = "An auspicious festival celebrating the birth anniversary of Lord Rama, the seventh avatar of Lord Vishnu, exemplifying righteousness.",
            ritualOrTip = "Participate in Ramayana recitals, decorate your house with rangoli, and distribute sweet Panakam (jaggery ginger drink)."
        ),
        Festival(
            name = "Mahavir Jayanti",
            dateStr = "March 31, 2026 (Tuesday)",
            month = 3,
            day = 31,
            type = FestivalType.GAZETTED,
            description = "One of the most important religious festivals in Jainism, celebrating the birth of Lord Mahavira, the 24th and last Tirthankara.",
            ritualOrTip = "Participate in silent prayers, visit Jain temples, donate to charitable trusts, and practice 'Ahimsa' (non-violence)."
        ),

        // April
        Festival(
            name = "Good Friday",
            dateStr = "April 03, 2026 (Friday)",
            month = 4,
            day = 3,
            type = FestivalType.GAZETTED,
            description = "A Christian holiday commemorating the crucifixion of Jesus Christ and his death at Calvary.",
            ritualOrTip = "Attend church services, read the Passion of Christ, and practice quiet contemplation or charity."
        ),
        Festival(
            name = "Baisakhi / Ambedkar Jayanti",
            dateStr = "April 14, 2026 (Tuesday)",
            month = 4,
            day = 14,
            type = FestivalType.REGIONAL,
            description = "Sikh New Year and spring Harvest Festival. Also marks the birth anniversary of Dr. B.R. Ambedkar, chief architect of the Indian Constitution.",
            ritualOrTip = "Perform lively Bhangra and Gidda dances, participate in colorful Nagar Kirtan processions, and share Kada Prasad."
        ),

        // May
        Festival(
            name = "Buddha Purnima / Buddha Jayanti",
            dateStr = "May 02, 2026 (Saturday)",
            month = 5,
            day = 2,
            type = FestivalType.GAZETTED,
            description = "An international Buddhist festival celebrating the birth, enlightenment (Nirvana), and passing of Gautama Buddha.",
            ritualOrTip = "Wear white garments, practice tranquil meditation, offer flowers to Buddha statues, and distribute free vegetarian food."
        ),
        Festival(
            name = "Eid al-Adha (Bakrid)",
            dateStr = "May 27, 2026 (Wednesday)",
            month = 5,
            day = 27,
            type = FestivalType.GAZETTED,
            description = "The Islamic 'Festival of Sacrifice' honoring Prophet Abraham's absolute willingness to sacrifice his son as an act of obedience to God.",
            ritualOrTip = "Offer morning congregational prayers, share festive meals with family, and help feed the poor and vulnerable."
        ),

        // June
        Festival(
            name = "Jagannath Ratha Yatra",
            dateStr = "June 16, 2026 (Tuesday)",
            month = 6,
            day = 16,
            type = FestivalType.REGIONAL,
            description = "The colossal chariot festival of Lord Jagannath, Lord Balabhadra, and Goddess Subhadra at Puri, Odisha, attracting millions of devotees.",
            ritualOrTip = "Offer prayers to Lord Jagannath, watch the grand chariot processions, and prepare or partake in 'Maha Prasad'."
        ),

        // July
        Festival(
            name = "Al-Hijra (Islamic New Year)",
            dateStr = "July 12, 2026 (Sunday)",
            month = 7,
            day = 12,
            type = FestivalType.RESTRICTED,
            description = "The starting day of the Hijri calendar, representing the historical migration of Prophet Muhammad from Mecca to Medina.",
            ritualOrTip = "Reflect on old lessons, pray for global peace, and spend quality time with loving friends."
        ),
        Festival(
            name = "Muharram (Ashura)",
            dateStr = "July 16, 2026 (Thursday)",
            month = 7,
            day = 16,
            type = FestivalType.GAZETTED,
            description = "A solemn day of mourning for Shi'a Muslims, remembering Imam Hussain ibn Ali (grandson of Prophet Muhammad) who was martyred at Karbala.",
            ritualOrTip = "Listen to Islamic historical narratives, observe fasts on the 9th and 10th of Muharram, and practice generous feeding."
        ),

        // August
        Festival(
            name = "Independence Day of India",
            dateStr = "August 15, 2026 (Saturday)",
            month = 8,
            day = 15,
            type = FestivalType.NATIONAL,
            description = "Commemorates the nation's independence from United Kingdom rule on 15th August 1947.",
            ritualOrTip = "Raise the Indian National Flag at schools or housing blocks, fly kites, and listen to patriotic speeches and songs."
        ),
        Festival(
            name = "Raksha Bandhan",
            dateStr = "August 27, 2026 (Thursday)",
            month = 8,
            day = 27,
            type = FestivalType.RESTRICTED,
            description = "A historic festival celebrating the sacred bond of protection, love, and care between brothers and sisters.",
            ritualOrTip = "Sisters tie a protective Rakhi thread on their brother's wrist, and brothers pledge support while gifting sweet treats."
        ),
        Festival(
            name = "Onam (Thiruvonam)",
            dateStr = "August 27, 2026 (Thursday)",
            month = 8,
            day = 27,
            type = FestivalType.REGIONAL,
            description = "The flagship cultural festival of Kerala, welcoming King Mahabali and celebrating nature's agricultural bounty.",
            ritualOrTip = "Design a dynamic 'Pookalam' floral rug at your entrance, and prepare a multi-dish traditional feast served on a banana leaf (Onasadhya)."
        ),

        // September
        Festival(
            name = "Janmashtami (Lord Krishna Birth)",
            dateStr = "September 04, 2026 (Friday)",
            month = 9,
            day = 4,
            type = FestivalType.GAZETTED,
            description = "The joyous celebration commemorating the earthly incarnation of Lord Krishna, representing the destroyer of darkness and symbol of divine love.",
            ritualOrTip = "Stay up until midnight (Krishna's birth hour), fast till midnight, sing devotional bhajans, and participate in local 'Dahi Handi' human pyramids."
        ),
        Festival(
            name = "Milad un-Nabi / Id-E-Milad",
            dateStr = "September 05, 2026 (Saturday)",
            month = 9,
            day = 5,
            type = FestivalType.GAZETTED,
            description = "Commemorates the birth anniversary of Prophet Muhammad, observed by Muslims around the globe with prayers and charity.",
            ritualOrTip = "Listen to discourses on the Prophet's exemplary life, recite sacred hymns, and feed the hungry."
        ),
        Festival(
            name = "Ganesh Chaturthi",
            dateStr = "September 14, 2026 (Monday)",
            month = 9,
            day = 14,
            type = FestivalType.RESTRICTED,
            description = "An eleven-day festival commemorating the birth of Lord Ganesha, the lord of new beginnings and remover of obstacles.",
            ritualOrTip = "Install an eco-friendly clay Ganesha statue, offer delicious steamed sweet dumplings (Modaks), and chant 'Ganpati Bappa Morya!'"
        ),

        // October
        Festival(
            name = "Mahatma Gandhi Jayanti",
            dateStr = "October 02, 2026 (Friday)",
            month = 10,
            day = 2,
            type = FestivalType.NATIONAL,
            description = "National Holiday honoring the birth anniversary of Mahatma Gandhi, the 'Father of the Nation', advocating Non-violence and Truth.",
            ritualOrTip = "Spend time doing community service, read about India's freedom struggle, and practice peaceful, constructive action."
        ),
        Festival(
            name = "Durga Ashtami (Durga Puja)",
            dateStr = "October 18, 2026 (Sunday)",
            month = 10,
            day = 18,
            type = FestivalType.REGIONAL,
            description = "The peak day of Durga Puja celebrating Goddess Durga's victorious battle over the shape-shifting buffalo demon Mahishasura.",
            ritualOrTip = "Visit colorfully decorated local pandals, offer pushpanjali (flower prayers) in the morning, and play dhunuchi-aarati."
        ),
        Festival(
            name = "Dussehra / Vijayadashami",
            dateStr = "October 20, 2026 (Tuesday)",
            month = 10,
            day = 20,
            type = FestivalType.GAZETTED,
            description = "A historic cultural event celebrating Dussehra (victory of Lord Rama over Ravana) and Vijayadashami (Goddess Durga's victory).",
            ritualOrTip = "Attend standard Ramleela theatrical shows, watch effigies of Ravana burn to symbolise evil's end, and start new learning paths."
        ),
        Festival(
            name = "Karwa Chauth",
            dateStr = "October 29, 2026 (Thursday)",
            month = 10,
            day = 29,
            type = FestivalType.RESTRICTED,
            description = "A traditional festival primarily celebrated by married women fasting from sunrise to moonrise praying for the long life of their spouse.",
            ritualOrTip = "Fast without water, listen to sacred Karwa Chauth Katha, look at the full moon through a sieve, and then drink your first water."
        ),

        // November
        Festival(
            name = "Diwali / Deepavali (Festival of Lights)",
            dateStr = "November 08, 2026 (Sunday)",
            month = 11,
            day = 8,
            type = FestivalType.GAZETTED,
            description = "India's massive Festival of Lights, signifying the return of Lord Rama to Ayodhya after 14 years of exile and the worship of Goddess Lakshmi.",
            ritualOrTip = "Clean the home thoroughly, light rows of clay oil lamps (Diyas) at entrances, design colorful Rangoli, and distribute sweets."
        ),
        Festival(
            name = "Govardhan Puja / Vishwakarma Day",
            dateStr = "November 09, 2026 (Monday)",
            month = 11,
            day = 9,
            type = FestivalType.RESTRICTED,
            description = "Honors the day Lord Krishna lifted the Govardhan Hill on his single little finger to shield the villagers from Lord Indra's deluge.",
            ritualOrTip = "Prepare a mound of mixed grain food representing the Govardhan Hill ('Annakut') to offer to deities with gratitude."
        ),
        Festival(
            name = "Chhath Puja",
            dateStr = "November 15, 2026 (Sunday)",
            month = 11,
            day = 15,
            type = FestivalType.REGIONAL,
            description = "An ancient Hindu festival dedicated to Surya (the Sun God) and Chhathi Maiya to express gratitude for life's basic sustainers.",
            ritualOrTip = "Perform holy baths at local riverbanks during sunrise and sunset, and prepare traditional 'Thekua' sweet offerings."
        ),
        Festival(
            name = "Guru Nanak Jayanti (Gurpurab)",
            dateStr = "November 24, 2026 (Tuesday)",
            month = 11,
            day = 24,
            type = FestivalType.GAZETTED,
            description = "Birth anniversary of Guru Nanak Dev Ji, founder of Sikhism and first of the ten Sikh Gurus, carrying messages of equality and love.",
            ritualOrTip = "Attend religious Prabhat Pheris at beautiful Gurudwaras, read the Guru Granth Sahib, and help cook or serve at Langars (free kitchens)."
        ),

        // December
        Festival(
            name = "Christmas Day",
            dateStr = "December 25, 2026 (Friday)",
            month = 12,
            day = 25,
            type = FestivalType.GAZETTED,
            description = "The globally celebrated Christian festival remembering the nativity of Jesus Christ, the Son of God, preaching love, mercy, and peace.",
            ritualOrTip = "Decorate a festive Christmas tree, share thoughtful gifts, sing traditional carols, and indulge in delicious plum cakes."
        )
    )

    fun getFestivalsForMonth(monthVal: Int): List<Festival> {
        return list2026.filter { it.month == monthVal }
    }
}
