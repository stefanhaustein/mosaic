package org.kobjects.mosaic.model.parser

import org.kobjects.parsek.tokenizer.Scanner
import org.kobjects.parsek.tokenizer.RegularExpressions

private val CELL_IDENTIFIER_REGEX = Regex("([a-zA-Z]+[a-zA-Z0-9]+!)?[a-zA-Z]+[0-9]+(:[a-zA-Z]+[0-9]+)?")
private val IDENTIFIER_REGEX = Regex("[a-zA-Z]+(_+[a-zA-Z0-9]*)*")
private val DOT_REGEX = Regex("\\.")

class TcScanner(
    input: String
) : Scanner<TcTokenType>(
    input,
    TcTokenType.EOF,
    RegularExpressions.WHITESPACE to null,
    CELL_IDENTIFIER_REGEX to TcTokenType.CELL_IDENTIFIER,
    IDENTIFIER_REGEX to TcTokenType.IDENTIFIER,
    RegularExpressions.UNSIGNED_NUMBER to TcTokenType.NUMBER,
    RegularExpressions.DOUBLE_QUOTED_STRING to TcTokenType.STRING,
    RegularExpressions.SYMBOL to TcTokenType.SYMBOL,
    DOT_REGEX to TcTokenType.SYMBOL
) {
}
