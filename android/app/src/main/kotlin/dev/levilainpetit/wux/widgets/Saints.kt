package dev.levilainpetit.wux.widgets

import java.time.LocalDate
import java.time.MonthDay

/**
 * Le calendrier des fêtes à souhaiter, un prénom par jour, tel que les
 * calendriers français l'impriment. Les fêtes religieuses sans prénom
 * gardent leur nom (« Épiphanie » les années où elle tombe un 6 janvier).
 */
object Saints {

    private val MONTHS = arrayOf(
        // Janvier
        arrayOf(
            "Marie", "Basile", "Geneviève", "Odilon", "Édouard", "Mélaine", "Raymond", "Lucien",
            "Alix", "Guillaume", "Paulin", "Tatiana", "Yvette", "Nina", "Rémi", "Marcel",
            "Roseline", "Prisca", "Marius", "Sébastien", "Agnès", "Vincent", "Barnard", "François de Sales",
            "Paul", "Paule", "Angèle", "Thomas d'Aquin", "Gildas", "Martine", "Marcelle",
        ),
        // Février
        arrayOf(
            "Ella", "Théophane", "Blaise", "Véronique", "Agathe", "Gaston", "Eugénie", "Jacqueline",
            "Apolline", "Arnaud", "Notre-Dame de Lourdes", "Félix", "Béatrice", "Valentin", "Claude", "Julienne",
            "Alexis", "Bernadette", "Gabin", "Aimée", "Damien", "Isabelle", "Lazare", "Modeste",
            "Roméo", "Nestor", "Honorine", "Romain", "Auguste",
        ),
        // Mars
        arrayOf(
            "Aubin", "Charles le Bon", "Guénolé", "Casimir", "Olive", "Colette", "Félicité", "Jean de Dieu",
            "Françoise", "Vivien", "Rosine", "Justine", "Rodrigue", "Mathilde", "Louise", "Bénédicte",
            "Patrice", "Cyrille", "Joseph", "Herbert", "Clémence", "Léa", "Victorien", "Catherine de Suède",
            "Humbert", "Larissa", "Habib", "Gontran", "Gwladys", "Amédée", "Benjamin",
        ),
        // Avril
        arrayOf(
            "Hugues", "Sandrine", "Richard", "Isidore", "Irène", "Marcellin", "Jean-Baptiste de la Salle", "Julie",
            "Gautier", "Fulbert", "Stanislas", "Jules", "Ida", "Maxime", "Paterne", "Benoît-Joseph",
            "Anicet", "Parfait", "Emma", "Odette", "Anselme", "Alexandre", "Georges", "Fidèle",
            "Marc", "Alida", "Zita", "Valérie", "Catherine de Sienne", "Robert",
        ),
        // Mai
        arrayOf(
            "Jérémie", "Boris", "Philippe", "Sylvain", "Judith", "Prudence", "Gisèle", "Désiré",
            "Pacôme", "Solange", "Estelle", "Achille", "Rolande", "Matthias", "Denise", "Honoré",
            "Pascal", "Éric", "Yves", "Bernardin", "Constantin", "Émile", "Didier", "Donatien",
            "Sophie", "Bérenger", "Augustin", "Germain", "Aymar", "Ferdinand", "Pétronille",
        ),
        // Juin
        arrayOf(
            "Justin", "Blandine", "Kévin", "Clotilde", "Igor", "Norbert", "Gilbert", "Médard",
            "Diane", "Landry", "Barnabé", "Guy", "Antoine de Padoue", "Élisée", "Germaine", "Jean-François Régis",
            "Hervé", "Léonce", "Romuald", "Silvère", "Rodolphe", "Alban", "Audrey", "Jean-Baptiste",
            "Prosper", "Anthelme", "Fernand", "Irénée", "Pierre et Paul", "Martial",
        ),
        // Juillet
        arrayOf(
            "Thierry", "Martinien", "Thomas", "Florent", "Antoine", "Mariette", "Raoul", "Thibaut",
            "Amandine", "Ulrich", "Benoît", "Olivier", "Henri et Joël", "Camille", "Donald", "Notre-Dame du Mont-Carmel",
            "Charlotte", "Frédéric", "Arsène", "Marina", "Victor", "Marie-Madeleine", "Brigitte", "Christine",
            "Jacques", "Anne et Joachim", "Nathalie", "Samson", "Marthe", "Juliette", "Ignace de Loyola",
        ),
        // Août
        arrayOf(
            "Alphonse", "Julien Eymard", "Lydie", "Jean-Marie Vianney", "Abel", "Transfiguration", "Gaétan", "Dominique",
            "Amour", "Laurent", "Claire", "Clarisse", "Hippolyte", "Evrard", "Marie", "Armel",
            "Hyacinthe", "Hélène", "Jean Eudes", "Bernard", "Christophe", "Fabrice", "Rose de Lima", "Barthélemy",
            "Louis", "Natacha", "Monique", "Augustin", "Sabine", "Fiacre", "Aristide",
        ),
        // Septembre
        arrayOf(
            "Gilles", "Ingrid", "Grégoire", "Rosalie", "Raïssa", "Bertrand", "Reine", "Nativité de Marie",
            "Alain", "Inès", "Adelphe", "Apollinaire", "Aimé", "Croix Glorieuse", "Roland", "Édith",
            "Renaud", "Nadège", "Émilie", "Davy", "Matthieu", "Maurice", "Constant", "Thècle",
            "Hermann", "Côme et Damien", "Vincent de Paul", "Venceslas", "Michel, Gabriel et Raphaël", "Jérôme",
        ),
        // Octobre
        arrayOf(
            "Thérèse de l'Enfant-Jésus", "Léger", "Gérard", "François d'Assise", "Fleur", "Bruno", "Serge", "Pélagie",
            "Denis", "Ghislain", "Firmin", "Wilfried", "Géraud", "Juste", "Thérèse d'Avila", "Edwige",
            "Baudouin", "Luc", "René", "Adeline", "Céline", "Élodie", "Jean de Capistran", "Florentin",
            "Crépin", "Dimitri", "Émeline", "Simon et Jude", "Narcisse", "Bienvenue", "Quentin",
        ),
        // Novembre
        arrayOf(
            "Toussaint", "Défunts", "Hubert", "Charles", "Sylvie", "Bertille", "Carine", "Geoffroy",
            "Théodore", "Léon", "Martin", "Christian", "Brice", "Sidoine", "Albert", "Marguerite",
            "Élisabeth", "Aude", "Tanguy", "Edmond", "Présentation de Marie", "Cécile", "Clément", "Flora",
            "Catherine", "Delphine", "Séverin", "Jacques de la Marche", "Saturnin", "André",
        ),
        // Décembre
        arrayOf(
            "Florence", "Viviane", "François-Xavier", "Barbara", "Gérald", "Nicolas", "Ambroise", "Immaculée Conception",
            "Pierre Fourier", "Romaric", "Daniel", "Jeanne-Françoise de Chantal", "Lucie", "Odile", "Ninon", "Alice",
            "Gaël", "Gatien", "Urbain", "Théophile", "Pierre Canisius", "Françoise-Xavière", "Armand", "Adèle",
            "Noël", "Étienne", "Jean", "Innocents", "David", "Roger", "Sylvestre",
        ),
    )

    /** Le prénom fêté le [date]. */
    fun of(date: LocalDate): String {
        val day = MonthDay.from(date)
        val month = MONTHS[day.monthValue - 1]
        return month.getOrElse(day.dayOfMonth - 1) { month.last() }
    }

    /** Vrai pour une fête religieuse plutôt qu'un prénom (pas de « Bonne fête »). */
    fun isFeast(name: String) = name in FEASTS

    private val FEASTS = setOf(
        "Notre-Dame de Lourdes", "Transfiguration", "Nativité de Marie", "Croix Glorieuse",
        "Toussaint", "Défunts", "Présentation de Marie", "Immaculée Conception", "Noël", "Innocents",
        "Notre-Dame du Mont-Carmel",
    )
}
