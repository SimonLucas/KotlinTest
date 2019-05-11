package games.eventqueuegame

/*
 A returned positive value is the number of surviving attackers; a negative value is interpreted as the number of
 surviving defenders, with the attack repulsed
 */
fun lanchesterLinearBattle(attack: Double, defence: Double, attackerDamageCoeff: Double, defenderDamageCoeff: Double): Double {
    var attackingForce = attack
    var defendingForce = defence
    var count = 0
    do {
        val attackDmg = attackingForce * attackerDamageCoeff
        val defenceDmg = defendingForce * defenderDamageCoeff
        attackingForce -= defenceDmg
        defendingForce -= attackDmg
        count++
    } while (attackingForce > 0.0 && defendingForce > 0.0 && count < 100)
    return if (defendingForce > 0.0) -defendingForce else attackingForce
}

fun lanchesterClosedFormBattle(attack: Double, defence: Double, attCoeff: Double, attExp: Double, defCoeff: Double, defExp: Double): Double {
    // firstly calculate which side will win
    val constant: Double = attCoeff * Math.pow(attack, attExp + 1) - defCoeff * Math.pow(defence, defExp + 1)
    if (constant > 0.0) {
        // attacker wins
        return Math.pow(constant / attCoeff, 1.0 / (attExp + 1.0))
    } else {
        // defender wins
        return -Math.pow(-constant / defCoeff, 1.0 / (defExp + 1.0))
    }
}