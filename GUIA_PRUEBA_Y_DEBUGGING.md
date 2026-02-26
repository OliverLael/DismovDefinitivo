# Guía de Prueba y Debugging

## 🧪 Cómo Probar los Cambios

### Paso 1: Verificar Permisos de Ubicación
1. Abre la app en tu dispositivo/emulador
2. Ve a **Configuración del dispositivo** → **Privacidad** → **Ubicación**
3. Busca "Trail Explorer" (o "MisLugares")
4. Selecciona **"Permitir solo mientras se usa la app"** o **"Permitir siempre"**

### Paso 2: Crear Lugares de Prueba (Opcional)
1. Abre la app
2. Ve a **"Mis Lugares"** (tarjeta inferior izquierda)
3. Haz clic en **"+"** para agregar un lugar
4. Completa:
   - Nombre: "Mi Restaurante Favorito"
   - Tipo: Restaurante
   - Posición: tu ubicación actual
5. Guarda y vuelve atrás

### Paso 3: Ir a Lugares de Interés
1. Desde el Dashboard, haz clic en **"Lugares de Interés"**
2. Espera a que aparezcan:
   - **Sección 1 (arriba)**: Tus lugares guardados locales
   - **Sección 2 (abajo)**: Lugares desde OpenStreetMap cercanos

### Paso 4: Verificar Clima
1. En el Dashboard (MainActivity)
2. En la parte superior, verás el clima actual con:
   - Temperatura en °C
   - Nombre de tu ciudad
   - Descripción del clima

## 🔍 Debugging - Qué Hacer Si No Ves Lugares

### Opción 1: Revisar Logs en Android Studio

```bash
# En Android Studio, abre la ventana Logcat:
# Android Studio → View → Tool Windows → Logcat

# Busca estos mensajes:
adb logcat | grep "LugaresInteres"
adb logcat | grep "Firebase"
adb logcat | grep "Weather"
```

### Opción 2: Verificar Configuración

1. Abre la app
2. Ve a **Configuración** (engranaje abajo a la derecha)
3. Verifica:
   - **"Tipo de guardado"**: ¿Local o Nube?
   - **"Máximo de lugares"**: Debe ser > 0
   - **"Orden"**: Selecciona "Por nombre"

### Opción 3: Resetear App

Si todo falla:
1. Android Studio → Build → Clean Project
2. Build → Rebuild Project
3. Run → Run 'app'

## 🌍 Probar APIs Directamente

### Probar API de Clima (Open-Meteo)

Abre en tu navegador:
```
https://api.open-meteo.com/v1/forecast?latitude=40.4168&longitude=-3.7038&current=temperature_2m,weather_code
```

Esperado (respuesta JSON):
```json
{
  "current": {
    "temperature_2m": 15.2,
    "weather_code": 0
  }
}
```

### Probar API de Lugares (Overpass)

```bash
curl -X POST "https://overpass-api.de/api/interpreter" \
  -d "data=[out:json][timeout:15];(node[\"amenity\"~\"restaurant|cafe\"][bbox:40.3,40.5,-3.8,-3.6]);out 60;"
```

## 🆘 Solucionar Problemas Comunes

### ❌ "No hay lugares de interés en el rango seleccionado"

**Causa probable:** El rango (5km, 10km, etc.) es demasiado pequeño

**Solución:**
1. Desde el Dashboard, aumenta el rango a **50km**
2. Vuelve a "Lugares de Interés"
3. Los lugares OSM deberían aparecer

### ❌ "Debes permitir el acceso a tu ubicación"

**Causa probable:** Permiso de ubicación no otorgado

**Solución:**
1. Ve a **Configuración del dispositivo** → **Apps** → **Trail Explorer**
2. Toca **"Permisos"** → **"Ubicación"**
3. Selecciona **"Permitir solo mientras se usa la app"**
4. Reinicia la app

### ❌ "Error al cargar lugares cercanos"

**Causa probable:** 
- Sin conexión a internet
- API de Overpass caída
- Timeout de red

**Solución:**
1. Verifica tu conexión WiFi/móvil
2. Espera 30 segundos
3. Vuelve atrás y entra nuevamente a "Lugares de Interés"

### ❌ Clima muestra "Sin datos"

**Causa probable:**
- Sin conexión a internet
- API de Open-Meteo caída
- Ubicación incorrecta

**Solución:**
1. Verifica conexión internet
2. Asegúrate que GPS está activado
3. Espera a que se detecte tu ubicación

## 📊 Información Técnica

### Estructura de Datos de Lugar Local
```kotlin
data class Lugar(
    var id: String,
    val nombre: String,
    val posicion: GeoPunto,      // Latitud, Longitud
    val tipo: TipoLugar,         // RESTAURANTE, BAR, etc.
    val dificultad: Dificultad,
    val valoracion: Float,
    val comentario: String?,
    // ... otros campos
)
```

### Tipos Considerados "Lugares de Interés"
```kotlin
setOf(
    TipoLugar.RESTAURANTE,    // 🍽️
    TipoLugar.BAR,             // 🍺
    TipoLugar.COMPRAS,         // 🛍️
    TipoLugar.ESPECTACULO,     // 🎭
    TipoLugar.HOTEL,           // 🏨
    TipoLugar.EDUCACION,       // 🎓
    TipoLugar.COPAS,           // 🍷
    TipoLugar.GASOLINERA       // ⛽
)
```

### Categorías OSM Soportadas
```
Restaurante (🍽️)
Café (☕)
Bar (🍺)
Cervecería (🍻)
Gasolinera (⛽)
Farmacia (💊)
Hospital (🏥)
Educación (🎓)
Biblioteca (📚)
Hotel (🏨)
Alojamiento (🛏️)
Museo (🏛️)
Atracción (🗺️)
Parque (🌳)
Deporte (⚽)
Tienda (🛍️)
```

## 📝 Contacto / Soporte

Si encuentras más problemas:
1. Revisa los logs en Logcat
2. Asegúrate de permisos y ubicación activa
3. Prueba en un rango más grande de distancia
4. Reinicia la app completamente

