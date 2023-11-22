package com.example.casefiles

data class UserDetails(
    val name: String = "",
    val nationalId: String = "",
    val phoneNumber: String = "",
    val residence: String = "",
)

data class PoliceDetails(
    val name: String = "",
    val serviceNumber: String = "",
    val phoneNumber: String = "",
    val station: String = "",
    )

data class Report(
    val location: String = "",
    val station: String = "",
    val date: String = "",
    val time: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val description: String = "",
    val userId: String = "",
    val status: String = ""
)

data class Update(

    val station: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val userId: String = ""

)

