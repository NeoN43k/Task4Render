package com.cgvsu.render_engine;

public enum RenderModes {
    WIREFRAME,          // Только сетка
    SOLID_COLOR,        // Заливка цветом
    TEXTURED,           // Только текстура
    LIT_SOLID,          // Освещение + цвет
    LIT_TEXTURED,       // Освещение + текстура
    FULL                // Все эффекты + сетка
}