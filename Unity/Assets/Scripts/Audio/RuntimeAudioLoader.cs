using System;
using System.Collections;
using UnityEngine;
using UnityEngine.Networking;

namespace PeterDrummer.Audio
{
    public class RuntimeAudioLoader : MonoBehaviour
    {
        public void LoadFromAbsolutePath(string absolutePath, Action<AudioClip> onLoaded, Action<string> onError)
        {
            StartCoroutine(LoadRoutine(absolutePath, onLoaded, onError));
        }

        private IEnumerator LoadRoutine(string absolutePath, Action<AudioClip> onLoaded, Action<string> onError)
        {
            if (string.IsNullOrWhiteSpace(absolutePath))
            {
                onError?.Invoke("Caminho de áudio inválido.");
                yield break;
            }

            string url = $"file://{absolutePath}";
            AudioType audioType = absolutePath.EndsWith(".wav", StringComparison.OrdinalIgnoreCase)
                ? AudioType.WAV
                : AudioType.MPEG;

            using UnityWebRequest req = UnityWebRequestMultimedia.GetAudioClip(url, audioType);
            yield return req.SendWebRequest();

            if (req.result != UnityWebRequest.Result.Success)
            {
                onError?.Invoke($"Falha ao carregar áudio: {req.error}");
                yield break;
            }

            AudioClip clip = DownloadHandlerAudioClip.GetContent(req);
            onLoaded?.Invoke(clip);
        }
    }
}
