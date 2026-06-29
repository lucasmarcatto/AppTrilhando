package com.trilhando.helper

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object PermissionHelper { //gerenciar permissoes

    //lista de permissões necessárias para a Sprint 4
    val PERMISSOES_LOCALIZACAO = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val PERMISSOES_PASSOS = arrayOf(
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    //combinação de todas para solicitar de uma vez
    val TODAS_PERMISSOES = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS
    )


    fun hasAllPermissions(activity: Activity): Boolean { //verifica se todas as permissões foram aceitas
        return TODAS_PERMISSOES.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }



    fun requestPermissions(activity: Activity, requestCode: Int) { //solicita permissões
        ActivityCompat.requestPermissions(activity, TODAS_PERMISSOES, requestCode)
    }

    //verifica se uma permissão específica foi concedida
    fun hasPermission(activity: Activity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
}