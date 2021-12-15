package model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionsList(val questionsList: List<Question>)