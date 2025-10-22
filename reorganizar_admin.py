#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script de reorganización automática USER - CanchaYA
Reorganiza la carpeta ui/user en subcarpetas modulares
"""

import os
import shutil
import re
from pathlib import Path

# ========================================
# CONFIGURACIÓN
# ========================================

BASE_PATH = Path("app/src/main/java/com/rojassac/canchaya/ui/user")
BASE_PACKAGE = "com.rojassac.canchaya.ui.user"

# Mapeo: archivo -> (carpeta_destino, nuevo_package)
FILE_MAPPING = {
    # HOME (Explorar canchas)
    "HomeFragment.kt": ("home", f"{BASE_PACKAGE}.home"),
    "CanchaAdapter.kt": ("home", f"{BASE_PACKAGE}.home"),
    "DetalleCanchaFragment.kt": ("home", f"{BASE_PACKAGE}.home"),
    
    # RESERVAS
    "ReservaFragment.kt": ("reservas", f"{BASE_PACKAGE}.reservas"),
    "ReservasFragment.kt": ("reservas", f"{BASE_PACKAGE}.reservas"),
    "ReservaAdapter.kt": ("reservas", f"{BASE_PACKAGE}.reservas"),
    "DetalleReservaFragment.kt": ("reservas", f"{BASE_PACKAGE}.reservas"),
    "HorarioAdapter.kt": ("reservas", f"{BASE_PACKAGE}.reservas"),
    
    # RESEÑAS
    "ResenasCanchaFragment.kt": ("resenas", f"{BASE_PACKAGE}.resenas"),
    "EscribirResenaFragment.kt": ("resenas", f"{BASE_PACKAGE}.resenas"),
    "ResenaAdapter.kt": ("resenas", f"{BASE_PACKAGE}.resenas"),
    
    # PAGOS
    "PagoFragment.kt": ("pagos", f"{BASE_PACKAGE}.pagos"),
    
    # PERFIL
    "PerfilFragment.kt": ("perfil", f"{BASE_PACKAGE}.perfil"),
    
    # MAPA
    "MapFragment.kt": ("mapa", f"{BASE_PACKAGE}.mapa"),
}

# Archivo que queda en la raíz (NO se mueve)
ROOT_FILES = ["UserViewModel.kt"]

# ========================================
# FUNCIONES AUXILIARES
# ========================================

def create_folders():
    """Crear estructura de carpetas"""
    print("📁 Creando estructura de carpetas...")
    folders = set(folder for folder, _ in FILE_MAPPING.values())
    
    for folder in folders:
        folder_path = BASE_PATH / folder
        folder_path.mkdir(exist_ok=True)
        print(f"   ✓ {folder}/")

def update_package_in_file(file_path, new_package):
    """Actualiza el package declaration en un archivo"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Reemplazar la línea de package
    updated_content = re.sub(
        r'^package\s+[\w.]+',
        f'package {new_package}',
        content,
        count=1,
        flags=re.MULTILINE
    )
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(updated_content)

def move_and_update_file(filename, folder, new_package):
    """Mover archivo y actualizar su package"""
    source = BASE_PATH / filename
    dest_folder = BASE_PATH / folder
    dest = dest_folder / filename
    
    if not source.exists():
        print(f"   ⚠️  {filename} no encontrado, saltando...")
        return False
    
    # Copiar archivo
    shutil.copy2(source, dest)
    
    # Actualizar package
    update_package_in_file(dest, new_package)
    
    # Eliminar original
    source.unlink()
    
    print(f"   ✓ {filename} → {folder}/")
    return True

def update_imports_in_project():
    """Actualizar imports en todo el proyecto"""
    print("\n🔄 Actualizando imports en el proyecto...")
    
    # Mapeo de imports antiguos a nuevos
    import_mapping = {
        f"{BASE_PACKAGE}.HomeFragment": f"{BASE_PACKAGE}.home.HomeFragment",
        f"{BASE_PACKAGE}.CanchaAdapter": f"{BASE_PACKAGE}.home.CanchaAdapter",
        f"{BASE_PACKAGE}.DetalleCanchaFragment": f"{BASE_PACKAGE}.home.DetalleCanchaFragment",
        f"{BASE_PACKAGE}.ReservaFragment": f"{BASE_PACKAGE}.reservas.ReservaFragment",
        f"{BASE_PACKAGE}.ReservasFragment": f"{BASE_PACKAGE}.reservas.ReservasFragment",
        f"{BASE_PACKAGE}.ReservaAdapter": f"{BASE_PACKAGE}.reservas.ReservaAdapter",
        f"{BASE_PACKAGE}.DetalleReservaFragment": f"{BASE_PACKAGE}.reservas.DetalleReservaFragment",
        f"{BASE_PACKAGE}.HorarioAdapter": f"{BASE_PACKAGE}.reservas.HorarioAdapter",
        f"{BASE_PACKAGE}.ResenasCanchaFragment": f"{BASE_PACKAGE}.resenas.ResenasCanchaFragment",
        f"{BASE_PACKAGE}.EscribirResenaFragment": f"{BASE_PACKAGE}.resenas.EscribirResenaFragment",
        f"{BASE_PACKAGE}.ResenaAdapter": f"{BASE_PACKAGE}.resenas.ResenaAdapter",
        f"{BASE_PACKAGE}.PagoFragment": f"{BASE_PACKAGE}.pagos.PagoFragment",
        f"{BASE_PACKAGE}.PerfilFragment": f"{BASE_PACKAGE}.perfil.PerfilFragment",
        f"{BASE_PACKAGE}.MapFragment": f"{BASE_PACKAGE}.mapa.MapFragment",
    }
    
    # Buscar todos los archivos .kt en ui/
    ui_path = Path("app/src/main/java/com/rojassac/canchaya/ui")
    kt_files = list(ui_path.rglob("*.kt"))
    
    files_updated = 0
    
    for kt_file in kt_files:
        try:
            with open(kt_file, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            
            # Reemplazar cada import
            for old_import, new_import in import_mapping.items():
                content = re.sub(
                    rf'import\s+{re.escape(old_import)}\b',
                    f'import {new_import}',
                    content
                )
            
            # Si hubo cambios, guardar
            if content != original_content:
                with open(kt_file, 'w', encoding='utf-8') as f:
                    f.write(content)
                files_updated += 1
                print(f"   ✓ {kt_file.relative_to(ui_path)}")
        
        except Exception as e:
            print(f"   ⚠️  Error en {kt_file.name}: {e}")
    
    print(f"\n   📝 {files_updated} archivos actualizados")

def show_final_structure():
    """Mostrar estructura final"""
    print("\n📂 Nueva estructura:")
    print("ui/user/")
    print("├── UserViewModel.kt")
    
    folders = sorted(set(folder for folder, _ in FILE_MAPPING.values()))
    for i, folder in enumerate(folders):
        prefix = "└──" if i == len(folders) - 1 else "├──"
        print(f"{prefix} {folder}/")
        
        files = [f for f, (fld, _) in FILE_MAPPING.items() if fld == folder]
        for j, file in enumerate(files):
            file_prefix = "    └──" if j == len(files) - 1 else "    ├──"
            print(f"{file_prefix} {file}")

# ========================================
# MAIN
# ========================================

def main():
    print("=" * 60)
    print("🚀 REORGANIZACIÓN AUTOMÁTICA USER - CANCHAYA")
    print("=" * 60)
    print()
    
    # Verificar que estamos en la raíz del proyecto
    if not BASE_PATH.exists():
        print("❌ Error: No se encontró la carpeta ui/user")
        print(f"   Ruta esperada: {BASE_PATH}")
        print("\n💡 Asegúrate de ejecutar el script desde la raíz del proyecto")
        return
    
    print(f"📍 Trabajando en: {BASE_PATH.absolute()}\n")
    
    # Paso 1: Crear carpetas
    create_folders()
    
    # Paso 2: Mover y actualizar archivos
    print("\n🔄 Moviendo archivos y actualizando packages...")
    moved_count = 0
    
    for filename, (folder, new_package) in FILE_MAPPING.items():
        if move_and_update_file(filename, folder, new_package):
            moved_count += 1
    
    print(f"\n   📦 {moved_count} archivos movidos")
    
    # Paso 3: Actualizar imports en todo el proyecto
    update_imports_in_project()
    
    # Paso 4: Mostrar estructura final
    show_final_structure()
    
    print("\n" + "=" * 60)
    print("✅ REORGANIZACIÓN USER COMPLETADA EXITOSAMENTE")
    print("=" * 60)
    print("\n📌 PRÓXIMOS PASOS:")
    print("   1. Abre Android Studio")
    print("   2. Menú: Build → Clean Project")
    print("   3. Menú: Build → Rebuild Project")
    print("   4. Verifica que no haya errores de compilación")
    print("\n💡 Si hay errores, ejecuta: ./gradlew clean build")

if __name__ == "__main__":
    main()
