package com.shortendesign.k9keyboard

import com.shortendesign.k9keyboard.util.Key
import com.shortendesign.k9keyboard.util.KeyCodeMapping
import com.shortendesign.k9keyboard.util.LetterLayout
import com.shortendesign.k9keyboard.util.MissingLetterCode
import java.lang.StringBuilder

/**
 * Class to represent the physical key layout and the mapping from keys to letters
 */
class Keypad(
    // Map from Android keyCode to the Key that should be activated
    private val keyCodeMapping: KeyCodeMapping,
    // Map from keyboard Key to the letters/symbols associated with that key
    private val letterLayout: Map<Key, List<Char>>
) {
    // Map from letters to the Key pressed to access them
    private lateinit var letterKeyMap: Map<Char, Key>
    // Map to cache which keys are used to type alpha-numeric characters
    private lateinit var keyIsLetterMap: Map<Key, Boolean>

    init {
        initKeyMaps(letterLayout)
    }

    /**
     * Resolve Android keycode to our internal Key enum value
     */
    fun getKey(keyCode: Int): Key? {
        return keyCodeMapping.key(keyCode)
    }

    /**
     * Tell whether a key is mapped to alpha-numeric characters
     */
    fun isLetter(key: Key): Boolean {
        if (keyIsLetterMap[key] == true)
            return true

        return false
    }

    /**
     * Get the numeric value associated with a key.
     * Currently the convention is to store the numeric value first in the list of characters for
     * the key.
     */
    fun getDigit(key: Key): Char? {
        val letters = letterLayout[key] ?: return null
        return if (letters.isNotEmpty() && letters[0].isDigit()) letters[0] else null
    }

    // Is this the delete key?
    fun isDelete(key: Key): Boolean {
        // hard coded for now
        return key == Key.DELETE
    }

    // Is this the next-candidate key?
    fun isNext(key: Key): Boolean {
        // hard coded for now
        return key == Key.NEXT
    }

    // Is this the next-candidate key?
    fun isShift(key: Key): Boolean {
        // hard coded for now
        return key == Key.SHIFT
    }

    // Is this the space key?
    fun isSpace(key: Key): Boolean {
        // hard coded for now
        return key == Key.N0
    }

    // Is this a directional key?
    fun isDirection(key: Key): Boolean {
        return setOf(Key.LEFT, Key.RIGHT, Key.UP, Key.DOWN).contains(key)
    }

    /**
     * Get the series of key character values required to represent a word
     */
    fun getCodeForWord(word: String): String {
        val builder = StringBuilder()
        for (letter in word) {
            val code = codeForLetter(letter)
                ?: throw MissingLetterCode("No code found for '$letter'")
            builder.append(code)
        }
        return builder.toString()
    }

    /**
     * Get the Key character value required to represent a letter
     */
    private fun codeForLetter(letter: Char): Char? {
        return letterKeyMap[letter]?.code
    }

    /***
     * Map all the letters in the keypad layout to the Key pressed to access it
     */
    fun initKeyMaps(layout: Map<Key, List<Char>>): Map<Char, Key> {
        val letterKeyMap = HashMap<Char, Key>()
        val keyIsLetterMap = HashMap<Key, Boolean>()
        this.letterKeyMap = letterKeyMap
        this.keyIsLetterMap = keyIsLetterMap
        for ((key, characters) in layout) {
            for (char in characters) {
                letterKeyMap[char] = key
                // If this letter isn't in non-alphanumeric => it should be alphanumeric
                if (!LetterLayout.nonAlphaNumeric.contains(char)) {
                    // Support mapping the uppercase character as well
                    letterKeyMap[char.uppercaseChar()] = key
                    keyIsLetterMap[key] = true
                }
                else if (keyIsLetterMap[key] == null) {
                    keyIsLetterMap[key] = false
                }
            }
        }
        return letterKeyMap
    }
}
