package app.ultradev.uifiles.startup

import app.ultradev.uifiles.service.UIAnalysisService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class UIStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        project.getService(UIAnalysisService::class.java)
            ?.scheduleInitialScan()
    }
}
