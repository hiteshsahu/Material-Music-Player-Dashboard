package demo.view.mvp;

/**
 * Abstract presenter to work as base for every presenter created in the application. This
 * presenter
 * declares some methods to attach the fragment/activity lifecycle.
 *
 * @author Pedro Vicente Gómez Sánchez
 */
public class SongsPresenter implements BasePresenter {

    private final View defaultView;

    public SongsPresenter(View defaultViewObject) {
        this.defaultView = defaultViewObject;
    }

    @Override
    public void fillData() {


    }

    public void onStarted() {
        defaultView.showProgressDialog();
    }

    public void onSuccess() {

        defaultView.dismissProgressDialog();
        defaultView.render();

    }

    public void onError(String errorMessage) {
        defaultView.dismissProgressDialog();
        defaultView.showErrorDialog(errorMessage);
    }

    /**
     * Actions to perform on UI
     */
    public interface View {
        void render();

        void showProgressDialog();

        void dismissProgressDialog();

        void showErrorDialog(String message);
    }
}