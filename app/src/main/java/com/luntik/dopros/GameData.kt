package com.luntik.dopros

/** Question you ASK. Reply is what the suspect says back. */
data class AskOption(
    val question: String,
    val reply: String,
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
    val asks: List<AskOption>,
    val maxInfo: Float = 100f
)

object GameData {
    val investigatorNames = listOf(
        "Ivan Gromov",
        "Maria Volkova",
        "Alex Chernov",
        "Elena Moroz",
        "Dmitri Steel",
        "Anna Kriv",
        "Sergei Belov",
        "Olga Ten",
        "Nikita Razum",
        "Victor Nemoy"
    )

    val tutorialSuspects: List<Suspect> = listOf(
        Suspect(
            id = "paul",
            name = "Paul Novak",
            role = "Witness · human",
            intro = "Male, 34. Hands shake. Says he was just passing by.",
            asks = listOf(
                AskOption(
                    question = "Where were you the night of the disappearance?",
                    reply = "At a bar until three. Ask anyone.",
                    infoGain = 15f,
                    fearSubject = 5f,
                    flags = setOf("alibi_bar")
                ),
                AskOption(
                    question = "Did you know the missing person?",
                    reply = "Saw him in the yard a few times. That's it.",
                    infoGain = 18f,
                    fearSubject = 6f,
                    flags = setOf("knew")
                ),
                AskOption(
                    question = "Why is there blood on your sleeve?",
                    reply = "I… helped someone after a fight. Not what you think.",
                    infoGain = 30f,
                    fearSubject = 14f,
                    flags = setOf("fight")
                ),
                AskOption(
                    question = "Who else was nearby that night?",
                    reply = "Some woman. Irene, I think. Leave me alone.",
                    infoGain = 35f,
                    fearSubject = 18f,
                    flags = setOf("names_irene")
                )
            )
        ),
        Suspect(
            id = "irene",
            name = "Irene Savell",
            role = "Suspect · human",
            intro = "Female, 29. Too calm. Looks straight at you.",
            asks = listOf(
                AskOption(
                    question = "Paul named you. Are you Irene?",
                    reply = "Yes. And so what?",
                    infoGain = 20f,
                    fearSubject = 8f,
                    flags = setOf("confirm")
                ),
                AskOption(
                    question = "Where is the missing person now?",
                    reply = "Under the bridge. But I didn't kill anyone.",
                    infoGain = 40f,
                    fearSubject = 28f,
                    flags = setOf("location")
                ),
                AskOption(
                    question = "What was your motive?",
                    reply = "He threatened to talk about another case. Money. Fear.",
                    infoGain = 35f,
                    fearSubject = 18f,
                    flags = setOf("motive", "blackmail")
                ),
                AskOption(
                    question = "Will you cooperate?",
                    reply = "I'll sign whatever you need. Just… stop with the shock.",
                    infoGain = 28f,
                    fearSubject = -4f,
                    flags = setOf("confess")
                )
            )
        )
    )
}
