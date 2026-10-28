# GolloSmart Dakboard para Android TV, Fire TV y Shield

App nativa mínima que abre tu pantalla de GolloSmartDakboard a pantalla completa:

- Mantiene la pantalla encendida mientras está abierta.
- Aparece en el menú de apps de la TV con tu logo.
- Si no hay internet, muestra un aviso y reintenta cada 15 segundos.
- Menú: tecla **Menú**, o **mantener pulsado Atrás** (el control del Shield no tiene tecla Menú). Opciones: recargar, cambiar dirección, borrar datos y salir.
- **Atrás** dos veces seguidas sale de la app.

Funciona en Nvidia Shield, Fire TV con Fire OS (Fire TV Stick 4K Plus/Max, Cube, HD de 1.ª gen. y TVs con Fire TV), Google TV / Android TV, tablets y móviles Android.
**No** funciona en los Fire TV con Vega OS (4K Select, HD de 2.ª gen., 4K de 3.ª gen.): no admiten APK.

## Antes de empezar

La dirección que abre la app está en `app/src/main/res/values/strings.xml` (`default_url`). Ya trae la dirección de tu página. **Esa página debe estar compartida con acceso por enlace** (menú *Compartir* de la página); si no, la TV pedirá iniciar sesión.

Si algún día cambia la dirección, edita ese archivo o usa el menú de la app (*Cambiar dirección*).

## Paso 1: obtener el APK (sin instalar nada en tu computadora)

GitHub lo compila gratis en la nube.

1. Crea una cuenta en <https://github.com> si no tienes.
2. Crea un repositorio nuevo, **público** (así el enlace de descarga funciona sin iniciar sesión en la TV). Nómbralo, por ejemplo, `gollosmart-tv`.
3. Sube el contenido de esta carpeta al repositorio: *Add file → Upload files* y arrastra **todo lo que hay dentro de la carpeta**.
   - **Importante:** la carpeta oculta `.github` debe subirse también. Si no aparece al arrastrar, crea el archivo a mano: *Add file → Create new file*, escribe como nombre `.github/workflows/build.yml` y pega el contenido del archivo del mismo nombre.
4. Abre la pestaña **Actions**. La compilación arranca sola (si no, elige *Compilar APK → Run workflow*). Tarda unos 3 a 6 minutos.
5. Cuando termine con una marca verde, abre la sección **Releases** (columna derecha del repositorio). Ahí queda el archivo `GolloSmartDakboard.apk` con un enlace fijo:

   `https://github.com/TU_USUARIO/TU_REPOSITORIO/releases/download/latest/GolloSmartDakboard.apk`

   Cada vez que cambies algo en el repositorio se compila de nuevo y ese enlace sirve la versión más reciente.

Si prefieres Android Studio: ábrelo, elige *Open* en esta carpeta y usa *Build → Build APK(s)*.

## Paso 2: instalar en la TV

### Con la app Downloader (la más sencilla)

1. Instala **Downloader** (de AFTVnews): en Fire TV desde la tienda de Amazon, en Shield desde Google Play.
2. Permite instalar apps desconocidas para Downloader:
   - Fire TV: *Configuración → Mi Fire TV → Opciones para desarrolladores → Instalar apps desconocidas → Downloader*.
   - Shield / Android TV: te lo pedirá al instalar (*Configuración → Seguridad y restricciones → Orígenes desconocidos*).
3. Abre Downloader, escribe el enlace del APK del paso 1 y confirma. Instala y abre.

### Con ADB (si ya lo usas)

```
adb connect IP_DE_LA_TV
adb install -r GolloSmartDakboard.apk
```

## Primer arranque

Se abre la página con tu logo. En una TV sin puntero, con las flechas ← → cambias de pantalla y **OK** abre el menú de la propia página.

## Si algo no funciona

- **Pantalla en blanco o pide iniciar sesión:** la página no está compartida por enlace.
- **La página se ve mal o no carga en un Fire TV viejo:** su WebView puede ser antiguo; actualiza el sistema del aparato.
- **El APK no se instala:** activa la instalación de apps desconocidas para la app que lo abre (Downloader).
- **Quieres que arranque sola al encender la TV:** Android no lo permite a las apps normales; déjala como última app abierta o crea una rutina con una app como *Launch on Boot* (opcional, aparte).
