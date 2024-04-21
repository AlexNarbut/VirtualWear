package ru.bestteam.virtualwear.app.main

import com.google.ar.core.Frame
import com.google.ar.core.Session

data class ArSessionFrame(val session: Session, val frame: Frame)