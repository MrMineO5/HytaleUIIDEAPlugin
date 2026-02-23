package app.ultradev.uifiles.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.APP, Service.Level.PROJECT)
class UIPluginDisposable : Disposable {
    companion object {
        fun getInstance(): Disposable = ApplicationManager.getApplication().getService(UIPluginDisposable::class.java)
        fun getInstance(project: Project): Disposable = project.getService(UIPluginDisposable::class.java)
    }

    override fun dispose() {
    }
}