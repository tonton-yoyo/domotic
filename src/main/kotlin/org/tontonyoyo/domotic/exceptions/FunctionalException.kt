package org.tontonyoyo.domotic.exceptions

import java.lang.RuntimeException

class FunctionalException(val errCode: Int, val msg: String): RuntimeException()