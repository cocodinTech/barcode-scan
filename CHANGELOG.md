## [1.2.0] 2024-01-30

### Changed
- Nueva clase ZebraMC33
- Invocar el metodo "enable" en el hilo principal

### Changed
- Actualizar libreria Zebra emdk.jar
  
## [1.1.26] 2024-01-30

### Fixed
- Habilitar el trigger analógico de los lectores Zebra en el método enable()

## [1.1.25] 2024-01-25

### Fixed
- Comentar la función initScanner() en el evento onOpened de los terminales Zebra para 
  evitar que se inicie el scanner dos veces para diferenetes devices.
- Añadir parámetro scannerIndex en el constructor de la clase ZebraMC33

## [1.1.24] 2023-12-26

### Added
- Clave 'format' en las lecturas de EA630 y ZebraMC33
- Adaptar clase ZebraMC33 a los lectores Android 11 (TC26)

## [1.1.23] 2023-06-12
### Fixed
 - No retornar las lecturas del EA630 si currentCallbackContext == null
 
## [1.1.19] 2023-02-23

### Changed
- Eliminar el '\n' de las lecturas del EA630
  
- ### Added
- Mediaplayer para reproducir el bep del lector cuando usamos camera


## [1.1.18] 2023-02-23

### Added
- Nuevo metodo para abrir los ajustes de notificaciones de Android

## [1.1.16] 2023-02-21

### Added
- Nuevo metodo para abrir la página de ajustes de Android para app

## [1.1.15] 2022-12-22

### Changed
- Recoger json con las opciones pasadas al metodo scan(). Solo si el dispositivo es 'camera'

## [1.1.14] 2022-03-7

### Added
- Nueva clase devices.IT51 para controlar el dispositivo ITOS IT_51

## [1.1.13] 2022-03-7

### Added
- Nueva clase devices.EA630 para controlar el dispositivo UnitechEA630

## [1.1.12] 2022-02-10

### Fixed
- Cambiado el modo de disparo en los terminales Zebra (de HARD a SOFT_ONCE)  para permitir lanzar el escaneo desde software
