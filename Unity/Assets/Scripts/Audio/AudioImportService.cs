using System;
using System.Collections;
using UnityEngine;

namespace PeterDrummer.Audio
{
    /// <summary>
    /// Abre seletor de arquivo no Android e devolve caminho absoluto.
    /// Requer plugin NativeFilePicker para Android.
    /// </summary>
    public class AudioImportService : MonoBehaviour
    {
        public void PickAudioFile(Action<string> onPathPicked, Action<string> onError)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            NativeFilePicker.Permission permission = NativeFilePicker.CheckPermission();
            if (permission != NativeFilePicker.Permission.Granted)
            {
                permission = NativeFilePicker.RequestPermission();
            }

            if (permission != NativeFilePicker.Permission.Granted)
            {
                onError?.Invoke("Permissão de armazenamento negada.");
                return;
            }

            NativeFilePicker.PickFile((path) =>
            {
                if (string.IsNullOrWhiteSpace(path))
                {
                    onError?.Invoke("Nenhum arquivo foi selecionado.");
                    return;
                }

                if (!path.EndsWith(".mp3", StringComparison.OrdinalIgnoreCase) &&
                    !path.EndsWith(".wav", StringComparison.OrdinalIgnoreCase))
                {
                    onError?.Invoke("Formato inválido. Use MP3 ou WAV.");
                    return;
                }

                onPathPicked?.Invoke(path);
            }, new[] { "audio/*" });
#else
            onError?.Invoke("File picker nativo disponível somente no Android build.");
#endif
        }
    }
}
