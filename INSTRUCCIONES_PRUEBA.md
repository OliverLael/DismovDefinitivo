# INSTRUCCIONES PARA PROBAR LOS CAMBIOS

## 🚀 Paso 1: Compilar y Ejecutar

```bash
# En Android Studio:
1. File → Sync Now (para sincronizar gradle)
2. Build → Clean Project
3. Build → Rebuild Project
4. Run → Run 'app' (o presiona Shift + F10)
```

## ✅ Paso 2: Configuración Inicial en el Dispositivo

### Permitir ubicación:
1. Abre la app
2. Si te pide permiso de ubicación, presiona **"Permitir"**
3. Si no aparece, ve a:
   - **Configuración** → **Apps** → **Trail Explorer**
   - **Permisos** → **Ubicación** → **Permitir**

### Habilitar GPS:
1. Ve a **Configuración** → **Ubicación**
2. Activa el interruptor
3. Selecciona **"Alta precisión"** (usa GPS + redes)

### Conectividad:
- Asegúrate de estar conectado a **WiFi o datos móviles**

## 📍 Paso 3: Crear Lugares de Prueba (Opcional)

Para ver lugares locales, primero crea algunos:

1. Desde el Dashboard, haz clic en **"Mis Lugares"**
2. Presiona el botón **"+"** (esquina inferior derecha)
3. Completa:
   - **Nombre**: "Mi Restaurante Favorito"
   - **Ubicación**: Tu ubicación actual (botón 📍)
   - **Tipo**: Restaurante
   - **Dificultad**: Principiante
   - Presiona **"GUARDAR"**
4. Repite para crear 2-3 lugares más

## 🍽️ Paso 4: Probar Lugares de Interés

1. Vuelve al **Dashboard**
2. Verifica que la sección de **Clima** tenga datos (temperatura y descripción)
3. Haz clic en **"Lugares de Interés"**

### Esperado:

**Arriba (Lugares Locales):**
- Muestra "Cargando lugares..."
- Después: Tus lugares guardados (si creaste alguno)

**Abajo (Lugares OpenStreetMap):**
- Muestra "Buscando lugares cercanos..."
- Barra de progreso
- Después: Lista de restaurantes, bares, hoteles, tiendas, etc. cercanos a ti

## 🎯 Paso 5: Interactuar con los Filtros

1. En la parte superior, verás chips (botones):
   - **Todos** (por defecto - muestra lugares de interés)
   - **Restaurante**
   - **Bar**
   - **Compras**
   - **Educación**
   - **Hotel**

2. Haz clic en cada uno para filtrar resultados

## 📊 Paso 6: Cambiar Rango de Búsqueda

1. Vuelve al **Dashboard**
2. En la parte superior derecha, verás chips con **"5km", "10km", "25km", "50km"**
3. Selecciona un rango diferente
4. Vuelve a **"Lugares de Interés"**
5. Verás nuevos resultados

## 🌡️ Paso 7: Verificar Clima

1. En el **Dashboard**, mira la sección superior
2. Deberías ver:
   - Temperatura en °C
   - Nombre de tu ciudad
   - Descripción del clima (Despejado, Nublado, Lluvia, etc.)

---

## 🔍 Debugging - Si Algo No Funciona

### Opción A: Ver Logs en Android Studio

```bash
# Abre Logcat:
# View → Tool Windows → Logcat

# Busca estos términos:
"LugaresInteres"  # Ubicación y lugares OSM
"Firebase"        # Base de datos
"Weather"         # Clima
```

### Opción B: Checklist de Verificación

- [ ] ¿La app tienes permiso de ubicación?
  - Configuración → Apps → Trail Explorer → Permisos → Ubicación ✓
  
- [ ] ¿El GPS del dispositivo está activado?
  - Configuración → Ubicación ✓
  
- [ ] ¿Estás conectado a Internet?
  - WiFi o datos móviles ✓
  
- [ ] ¿Esperaste a que cargue (barra de progreso desapareció)?
  - Espera 5-10 segundos ✓
  
- [ ] ¿El rango es lo suficientemente grande?
  - Intenta con 50km ✓
  
- [ ] ¿La app fue compilada después de los cambios?
  - Build → Rebuild Project ✓

### Opción C: Test Manual de APIs

#### Probar Clima:
```bash
curl "https://api.open-meteo.com/v1/forecast?latitude=40.4168&longitude=-3.7038&current=temperature_2m,weather_code"
```

Deberías ver un JSON con temperatura y código de clima.

#### Probar OpenStreetMap:
```bash
curl -X POST "https://overpass-api.de/api/interpreter" \
  -d "data=[out:json][timeout:15];(node[\"amenity\"~\"restaurant|cafe\"][bbox:40.3,40.5,-3.8,-3.6]);out 60;"
```

Deberías ver una lista de restaurantes en JSON.

---

## 🆘 Problemas Comunes y Soluciones

### ❌ "Debes permitir el acceso a tu ubicación"
- **Causa**: Permiso no otorgado
- **Solución**: Configuración → Apps → Trail Explorer → Permisos → Ubicación → Permitir

### ❌ "Cargando lugares..." pero no aparece nada
- **Causa**: Sin datos locales guardados o sin conexión
- **Solución**: 
  1. Crea algunos lugares en "Mis Lugares"
  2. Verifica tu conexión WiFi/datos
  3. Espera 10 segundos más

### ❌ "Error al cargar lugares cercanos"
- **Causa**: API de Overpass caída o sin internet
- **Solución**: 
  1. Verifica conexión a internet
  2. Espera unos minutos
  3. Intenta de nuevo

### ❌ Clima muestra "Sin datos"
- **Causa**: Sin ubicación detectada
- **Solución**:
  1. Activa el GPS del dispositivo
  2. Espera a que obtenga ubicación
  3. Vuelve a la app

### ❌ No hay ubicación (GPS no funciona)
- **Causa**: Permiso de ubicación no otorgado
- **Solución**:
  1. Configuración → Privacidad → Ubicación → Permitir

---

## 📝 Documentación de Referencia

En el proyecto encontrarás:

1. **RESUMEN_CAMBIOS.md** - Resumen ejecutivo de qué se cambió
2. **GUIA_PRUEBA_Y_DEBUGGING.md** - Guía completa de debugging
3. **ARREGLOS_REALIZADOS.md** - Detalles técnicos de cada cambio

---

## ✨ Características Implementadas

### Ubicación de Interés (Nueva)
- ✅ Búsqueda de lugares cercanos en OpenStreetMap
- ✅ Filtrado por categoría (Restaurante, Bar, Hotel, etc.)
- ✅ Ordenamiento por distancia
- ✅ Iconos emoji para cada categoría
- ✅ Manejo robusto de errores

### Lugares Locales (Mejorado)
- ✅ Filtrado por rango de km
- ✅ Mensajes de carga claros
- ✅ Sincronización con base de datos (local o Firebase)

### Clima (Existente)
- ✅ API Open-Meteo (gratis, sin API key)
- ✅ Temperatura en tiempo real
- ✅ Descripción del clima
- ✅ Actualización automática

---

## 📞 Notas Finales

- Todo funciona sin API Keys de pago
- Las APIs usadas son gratuitas y públicas
- No se guardan datos personales
- La ubicación se usa solo durante la sesión

¡Disfruta explorando lugares cercanos a ti! 🗺️

