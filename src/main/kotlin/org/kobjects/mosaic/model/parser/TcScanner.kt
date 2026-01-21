package org.kobjects.mosaic.model.parser

import org.kobjects.parsek.tokenizer.Scanner
import org.kobjects.parsek.tokenizer.RegularExpressions

class TcScanner(
    input: String
) : Scanner<TcTokenType>(
    input,
    TcTokenType.EOF,
    RegularExpressions.WHITESPACE to null,
    Regex("([a-zA-Z]+[a-zA-Z0-9]+!)?[a-zA-Z]+[0-9]+(:[a-zA-Z]+[0-9]+)?") to TcTokenType.CELL_IDENTIFIER,
    Regex("[a-zA-Z]+(_+[a-zA-Z0-9]*)*") to TcTokenType.IDENTIFIER,
    RegularExpressions.NUMBER to TcTokenType.NUMBER,
    RegularExpressions.DOUBLE_QUOTED_STRING to TcTokenType.STRING,
    RegularExpressions.SYMBOL to TcTokenType.SYMBOL,
    Regex("\\.") to TcTokenType.SYMBOL
) {
}