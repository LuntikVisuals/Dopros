package com.luntik.dopros

data class Question(
    val text: String,
    val answers: List<Answer>
)

data class Answer(
    val text: String,
    val infoGain: Float,
    val fearSubject: Float = 0f,
    val fearPlayer: Float = 0f,
    val flags: Set<String> = emptySet()
)

data class Suspect(
    val id: String,
    val name: String,
    val role: String,
    val intro: String,
    val questions: List<Question>,
    val maxInfo: Float = 100f
)

object GameData {
    val investigatorNames = listOf(
        "Иван Громов",
        "Мария Волкова",
        "Алексей Чёрный",
        "Елена Морозова",
        "Дмитрий Стальной",
        "Анна Кривцова",
        "Сергей Белов",
        "Ольга Тень",
        "Никита Разумов",
        "Виктор Немой"
    )

    val tutorialSuspects: List<Suspect> = listOf(
        Suspect(
            id = "pavel",
            name = "Павел Новиков",
            role = "Свидетель · человек",
            intro = "Мужчина 34 лет. Дрожат руки. Говорит, что просто проходил мимо.",
            questions = listOf(
                Question(
                    "Где вы были в ночь пропажи?",
                    listOf(
                        Answer("Дома. Смотрел телевизор.", 8f, fearSubject = 2f),
                        Answer("В баре до трёх.", 15f, fearSubject = 5f, flags = setOf("alibi_bar")),
                        Answer("Не помню. Напился.", 5f, fearSubject = 8f, fearPlayer = 2f)
                    )
                ),
                Question(
                    "Вы знали пропавшего?",
                    listOf(
                        Answer("Нет. Первый раз слышу.", 5f),
                        Answer("Видел пару раз во дворе.", 18f, fearSubject = 6f, flags = setOf("knew")),
                        Answer("Мы пересекались. По работе.", 25f, fearSubject = 12f, flags = setOf("knew", "work"))
                    )
                ),
                Question(
                    "Почему у вас кровь на рукаве?",
                    listOf(
                        Answer("Порезался на кухне!", 10f, fearSubject = 15f),
                        Answer("Это не кровь.", 5f, fearSubject = 20f, fearPlayer = 5f),
                        Answer("Я помогал человеку после драки.", 30f, fearSubject = 10f, flags = setOf("fight"))
                    )
                ),
                Question(
                    "Кто ещё был рядом той ночью?",
                    listOf(
                        Answer("Никого.", 5f),
                        Answer("Какая-то женщина. Ирина, кажется.", 35f, fearSubject = 18f, flags = setOf("names_irina")),
                        Answer("Я никого не сдам.", 0f, fearSubject = 25f, fearPlayer = 8f)
                    )
                )
            )
        ),
        Suspect(
            id = "irina",
            name = "Ирина Савельева",
            role = "Подозреваемая · человек",
            intro = "Женщина 29 лет. Спокойная, слишком спокойная. Смотрит прямо.",
            questions = listOf(
                Question(
                    "Вы Ирина? Павел назвал ваше имя.",
                    listOf(
                        Answer("Павел врёт.", 10f, fearSubject = 5f),
                        Answer("Да. И что?", 20f, fearSubject = 8f, flags = setOf("confirm")),
                        Answer("Он сам всё устроил.", 25f, fearSubject = 15f, flags = setOf("blame_pavel"))
                    )
                ),
                Question(
                    "Где пропавший сейчас?",
                    listOf(
                        Answer("Откуда мне знать?", 5f),
                        Answer("Если бы я знала — сказала бы.", 12f, fearSubject = 10f),
                        Answer("Под мостом. Но я не убивала.", 40f, fearSubject = 30f, flags = setOf("location"))
                    )
                ),
                Question(
                    "Зачем вам это было нужно?",
                    listOf(
                        Answer("Мне ничего не нужно.", 5f, fearPlayer = 5f),
                        Answer("Деньги. Только деньги.", 28f, fearSubject = 12f, flags = setOf("motive")),
                        Answer("Он угрожал рассказать о другом деле.", 35f, fearSubject = 20f, flags = setOf("motive", "blackmail"))
                    )
                ),
                Question(
                    "Вы будете сотрудничать?",
                    listOf(
                        Answer("У меня есть адвокат.", 5f, fearPlayer = 10f),
                        Answer("Да. Только уберите от меня ток.", 22f, fearSubject = 5f),
                        Answer("Я подпишу всё. Мне страшно.", 30f, fearSubject = -5f, flags = setOf("confess"))
                    )
                )
            )
        )
    )
}
