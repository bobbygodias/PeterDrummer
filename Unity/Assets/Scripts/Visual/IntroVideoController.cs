using System.Text.RegularExpressions;
using UnityEngine;
using UnityEngine.Video;

namespace PeterDrummer.Visual
{
    /// <summary>
    /// Controla vídeo de introdução (local ou URL) com opção de pular por toque.
    /// </summary>
    public class IntroVideoController : MonoBehaviour
    {
        [SerializeField] private VideoPlayer videoPlayer;
        [SerializeField] private GameObject introRoot;
        [SerializeField] private bool autoPlayOnStart = true;
        [SerializeField] private bool allowSkip = true;
        [SerializeField] private string localStreamingAssetFile = "intro.mp4";

        private bool _isPlaying;

        private void Awake()
        {
            if (videoPlayer != null)
            {
                videoPlayer.loopPointReached += OnVideoFinished;
            }
        }

        private void OnDestroy()
        {
            if (videoPlayer != null)
            {
                videoPlayer.loopPointReached -= OnVideoFinished;
            }
        }

        private void Start()
        {
            if (autoPlayOnStart)
            {
                PlayFromStreamingAssets();
            }
        }

        private void Update()
        {
            if (!allowSkip || !_isPlaying) return;

            if (Input.touchCount > 0 || Input.GetKeyDown(KeyCode.Space) || Input.GetMouseButtonDown(0))
            {
                Skip();
            }
        }

        public void PlayFromStreamingAssets()
        {
            if (videoPlayer == null) return;

            string url = $"{Application.streamingAssetsPath}/{localStreamingAssetFile}";
            videoPlayer.source = VideoSource.Url;
            videoPlayer.url = url;
            Play();
        }

        public void PlayFromUrl(string url)
        {
            if (videoPlayer == null || string.IsNullOrWhiteSpace(url)) return;

            videoPlayer.source = VideoSource.Url;
            videoPlayer.url = ConvertGoogleDriveShareToDirect(url);
            Play();
        }

        private void Play()
        {
            if (introRoot != null) introRoot.SetActive(true);
            _isPlaying = true;
            videoPlayer.Play();
        }

        public void Skip()
        {
            if (videoPlayer != null) videoPlayer.Stop();
            OnVideoFinished(null);
        }

        private void OnVideoFinished(VideoPlayer _)
        {
            _isPlaying = false;
            if (introRoot != null) introRoot.SetActive(false);
        }

        // Ex.: https://drive.google.com/file/d/<ID>/view?... -> https://drive.google.com/uc?export=download&id=<ID>
        private static string ConvertGoogleDriveShareToDirect(string input)
        {
            Match m = Regex.Match(input, @"/file/d/([a-zA-Z0-9_-]+)");
            if (!m.Success) return input;
            string id = m.Groups[1].Value;
            return $"https://drive.google.com/uc?export=download&id={id}";
        }
    }
}
