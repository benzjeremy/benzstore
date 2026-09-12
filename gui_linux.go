//go:build linux && cgo

package main

/*
#cgo pkg-config: webkit2gtk-4.1 gtk+-3.0
#include <stdlib.h>
#include <gtk/gtk.h>
#include <webkit2/webkit2.h>

static int check_display() {
    int argc = 0;
    char **argv = NULL;
    return gtk_init_check(&argc, &argv) ? 1 : 0;
}

static void on_window_destroy(GtkWidget *widget, gpointer data) {
    gtk_main_quit();
}

static gboolean on_context_menu(WebKitWebView *web_view, WebKitContextMenu *context_menu, GdkEvent *event, WebKitHitTestResult *hit_test_result, gpointer user_data) {
    return TRUE;
}

static void run_gtk_store(const char *title, const char *url, int width, int height) {
    int argc = 0;
    char **argv = NULL;
    if (!gtk_init_check(&argc, &argv)) {
        return;
    }

    g_set_prgname("benzstore");
    g_set_application_name("BenzStore");

    GtkWidget *window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    gtk_window_set_title(GTK_WINDOW(window), title);
    gtk_window_set_default_size(GTK_WINDOW(window), width, height);
    gtk_window_set_position(GTK_WINDOW(window), GTK_WIN_POS_CENTER);

    GdkRGBA bg_color;
    gdk_rgba_parse(&bg_color, "#0d1117");

    WebKitUserContentManager *content_manager = webkit_user_content_manager_new();
    WebKitSettings *settings = webkit_settings_new();
    webkit_settings_set_enable_developer_extras(settings, FALSE);
    webkit_settings_set_hardware_acceleration_policy(settings, WEBKIT_HARDWARE_ACCELERATION_POLICY_ALWAYS);

    GtkWidget *web_view = g_object_new(WEBKIT_TYPE_WEB_VIEW,
                                       "user-content-manager", content_manager,
                                       "settings", settings,
                                       NULL);

    webkit_web_view_set_background_color(WEBKIT_WEB_VIEW(web_view), &bg_color);
    g_signal_connect(web_view, "context-menu", G_CALLBACK(on_context_menu), NULL);

    GtkWidget *scrolled_window = gtk_scrolled_window_new(NULL, NULL);
    gtk_container_add(GTK_CONTAINER(scrolled_window), web_view);
    gtk_container_add(GTK_CONTAINER(window), scrolled_window);

    g_signal_connect(window, "destroy", G_CALLBACK(on_window_destroy), NULL);

    webkit_web_view_load_uri(WEBKIT_WEB_VIEW(web_view), url);

    gtk_widget_show_all(window);
    gtk_main();
}
*/
import "C"
import (
	"log"
	"os"
	"os/exec"
	"unsafe"
)

func LaunchGUI(title, url string, width, height int) {
	hasDisplay := os.Getenv("DISPLAY") != "" || os.Getenv("WAYLAND_DISPLAY") != ""
	if !hasDisplay || C.check_display() == 0 {
		log.Println("[GUI] Kein Display gefunden, öffne im Standard-Browser...")
		_ = exec.Command("xdg-open", url).Start()
		return
	}

	cTitle := C.CString(title)
	cURL := C.CString(url)
	defer C.free(unsafe.Pointer(cTitle))
	defer C.free(unsafe.Pointer(cURL))

	log.Printf("[GUI] Starte BenzStore native WebKitGTK Shell (%s)...\n", url)
	C.run_gtk_store(cTitle, cURL, C.int(width), C.int(height))
}
