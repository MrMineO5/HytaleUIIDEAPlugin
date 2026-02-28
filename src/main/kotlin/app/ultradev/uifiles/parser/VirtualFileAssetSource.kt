package app.ultradev.uifiles.parser

import app.ultradev.hytaleuiparser.source.AssetSource
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.Path

class VirtualFileAssetSource(val root: VirtualFile) : AssetSource {
    override fun listAllFiles(): List<Path> {
        val files = mutableListOf<Path>()
        VfsUtilCore.iterateChildrenRecursively(root, null) { file ->
            files.add(Path(VfsUtilCore.getRelativePath(file, root, '/') ?: return@iterateChildrenRecursively true))
            true
        }
        return files
    }

    override fun getAsset(path: Path): InputStream? {
        return root.findFileByRelativePath(path.toString())?.inputStream
    }
}