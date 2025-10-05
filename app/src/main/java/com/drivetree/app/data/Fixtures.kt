// app/src/main/java/com/drivetree/app/data/Fixtures.kt
package com.drivetree.app.data

object Fixtures {
    // Language codes used below (free-form strings are fine for our search):
    // EN=English, HI=Hindi, TA=Tamil, PA=Punjabi, GU=Gujarati, NE=Nepali,
    // FA=Dari/Farsi, PS=Pashto, UR=Urdu, FR=French, AR=Arabic, ES=Spanish, KO=Korean, RU=Russian, ZH=Chinese
    val instructors = listOf(
        // Existing/Canadian mix
        Instructor("1","Sara Ahmed","123 King St W, Toronto",45,4.9,"Toronto", listOf("EN","AR"), true, "Sedan"),
        Instructor("2","Leo Zhang","2000 Burnhamthorpe Rd W, Mississauga",40,4.6,"Mississauga", listOf("EN","ZH"), true, "SUV"),
        Instructor("3","Olivia Brown","1 Yonge St, Toronto",50,5.0,"Toronto", listOf("EN","FR"), true, "SUV"),

        // 🇮🇳 Indian names
        Instructor("10","Aarav Sharma","350 Bay St, Toronto",44,4.7,"Toronto", listOf("EN","HI"), true, "Sedan"),
        Instructor("11","Ananya Iyer","720 Kennedy Rd, Scarborough",39,4.3,"Scarborough", listOf("EN","TA"), false, "Hatchback"),
        Instructor("12","Rohit Verma","100 City Centre Dr, Mississauga",42,4.5,"Mississauga", listOf("EN","HI"), true, "Sedan"),
        Instructor("13","Simran Kaur","10 Queen St E, Brampton",41,4.6,"Brampton", listOf("EN","PA","HI"), true, "Sedan"),
        Instructor("14","Sanjay Patel","5000 Yonge St, North York",38,4.2,"North York", listOf("EN","GU","HI"), false, "Sedan"),

        // 🇳🇵 Nepali names
        Instructor("20","Sujita Gurung","55 Lawrence Ave E, Scarborough",37,4.4,"Scarborough", listOf("EN","NE"), true, "Hatchback"),
        Instructor("21","Prakash Adhikari","250 The East Mall, Etobicoke",40,4.3,"Etobicoke", listOf("EN","NE","HI"), false, "Sedan"),
        Instructor("22","Nabin Shrestha","15 York St, Toronto",43,4.5,"Toronto", listOf("EN","NE"), true, "SUV"),

        // 🇦🇫 Afghani names
        Instructor("30","Ahmad Wali","375 Dundas St E, Mississauga",39,4.2,"Mississauga", listOf("EN","FA","PS"), true, "Sedan"),
        Instructor("31","Farzana Ahmadi","365 Finch Ave W, North York",36,4.1,"North York", listOf("EN","FA"), false, "Sedan"),
        Instructor("32","Omid Rahimi","45 Overlea Blvd, Toronto",42,4.4,"Toronto", listOf("EN","PS","FA"), true, "Sedan"),
        Instructor("33","Zahra Popal","2150 Lawrence Ave E, Scarborough",35,4.0,"Scarborough", listOf("EN","FA"), false, "Hatchback"),

        // A few from your earlier set to keep variety
        Instructor("40","Mateo Silva","12 Peel Centre Dr, Brampton",42,4.7,"Brampton", listOf("EN","ES"), true, "Hatchback"),
        Instructor("41","Jae Park","250 The East Mall, Etobicoke",39,4.4,"Etobicoke", listOf("EN","KO"), true, "Sedan"),
        Instructor("42","Igor Petrov","365 Finch Ave W, North York",37,4.1,"North York", listOf("EN","RU"), false, "Sedan"),
        Instructor("43","Fatima Khan","700 Don Mills Rd, North York",35,4.0,"North York", listOf("EN","UR"), false, "Sedan")
    )
}
